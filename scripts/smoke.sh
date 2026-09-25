#!/usr/bin/env bash
# SPDX-License-Identifier: BSD-3-Clause
#
# arara's end-to-end smoke suite. One command that builds the runnable
# jar, provisions a scratch matrix from the repository fixtures, and
# runs the full scenario set on the underlying JVM. A second leg re-runs
# the matrix on the floor runtime named by the smoke properties, which
# is discovered among the usual caches or provisioned on demand through
# the build's runtime task, so the shipped artifact is executed on the
# very runtime it promises to support. Scenario outputs stay in the
# scratch tree for inspection. POSIX hosts only; Windows keeps the
# manual recipe.

set -u

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SMOKE="${SMOKE_DIR:-/tmp/arara-smoke}"
FIXTURES="${ROOT}/cli/src/test/resources/executiontests"
AUTHORED="${ROOT}/scripts/smoke"

FLOOR="$(sed -n 's/^smoke\.jvm\.version=//p' "${ROOT}/gradle.properties")"

JAR=""
APP_JAR=""
UNDERLYING_JAVA=""
FLOOR_JAVA=""
LEG=""
FAILURES=0
FAILURE_NAMES=()

say() { printf '%s\n' "$*"; }

check() {
    local description="$1" expected="$2" actual="$3"
    if [[ "${expected}" == "${actual}" ]]; then
        say "  PASS: ${description}"
    else
        say "  FAIL: ${description} — expected [${expected}], got [${actual}]"
        FAILURES=$((FAILURES + 1))
        FAILURE_NAMES+=("${description}")
    fi
}

count_in() {
    local needle="$1" file="$2" count
    count="$(grep -c -- "${needle}" "${file}" 2>/dev/null || true)"
    printf '%s' "${count:-0}"
}

java_major() {
    local candidate="$1" first
    first="$("${candidate}" -version 2>&1 | head -1 || true)"
    if [[ "${first}" =~ version\ \"([0-9]+)\. ]]; then
        printf '%s' "${BASH_REMATCH[1]}"
    fi
}

resolve_underlying() {
    if [[ -n "${SMOKE_JAVA:-}" ]]; then
        UNDERLYING_JAVA="${SMOKE_JAVA}"
    elif command -v java >/dev/null 2>&1; then
        UNDERLYING_JAVA="$(command -v java)"
    elif [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then
        UNDERLYING_JAVA="${JAVA_HOME}/bin/java"
    else
        say "FATAL: no Java runtime found; export SMOKE_JAVA to point at one"
        exit 1
    fi
    say "== underlying JVM: ${UNDERLYING_JAVA} ($("${UNDERLYING_JAVA}" -version 2>&1 | head -1))"
}

build_jar() {
    say "== building the runnable jar"
    ( cd "${ROOT}" && ./gradlew :cli:shadowJar --console=plain -q ) || {
        say "FATAL: the jar build failed"
        exit 1
    }
    JAR="$(ls -1t "${ROOT}"/cli/build/libs/arara-cli-with-deps-*.jar 2>/dev/null | head -1)"
    if [[ -z "${JAR}" ]]; then
        say "FATAL: no runnable jar was produced"
        exit 1
    fi
    APP_JAR="${SMOKE}/app/$(basename "${JAR}")"
    say "   ${JAR}"
}

provision_tree() {
    say "== provisioning the smoke tree at ${SMOKE}"
    mkdir -p "${SMOKE}/app/rules" \
             "${SMOKE}/run1" "${SMOKE}/run2" "${SMOKE}/run3" \
             "${SMOKE}/run4" "${SMOKE}/run5" \
             "${SMOKE}/tex-db" "${SMOKE}/tex-yaml" "${SMOKE}/tex-lua"
    cp -f "${FIXTURES}/simple-directive/"*         "${SMOKE}/run1/"
    cp -f "${FIXTURES}/lua-engine/"*               "${SMOKE}/run2/"
    cp -f "${FIXTURES}/lua-engine-conditionals/"*  "${SMOKE}/run3/"
    cp -f "${FIXTURES}/lua-engine-conditionals/"*  "${SMOKE}/run4/"
    cp -f "${FIXTURES}/lua-engine-options/"*       "${SMOKE}/run5/"
    cp -f "${AUTHORED}/dbcheck.tex"                 "${SMOKE}/tex-db/"
    cp -f "${AUTHORED}/pdflatex.tex"                "${SMOKE}/tex-yaml/tex-yaml.tex"
    cp -f "${AUTHORED}/pdflatex.tex"                "${SMOKE}/tex-lua/tex-lua.tex"
    # the official rules ship under the distribution prefix; the
    # application's rules directory expects the installed naming
    cp -f "${ROOT}/rules/arara-rule-pdflatex.yaml"    "${SMOKE}/app/rules/pdflatex.yaml"
    cp -f "${ROOT}/rules/arara-rule-pdflatex.lua"    "${SMOKE}/app/rules/pdflatex.lua"
    cp -f "${FIXTURES}/lua-engine-options/optechoer.lua" "${SMOKE}/app/rules/"
    cp -f "${JAR}"                                  "${SMOKE}/app/"
}

run1() {
    local java="$1" out rc
    out="${SMOKE}/run1/out.txt"
    ( cd "${SMOKE}/run1" && "${java}" -jar "${APP_JAR}" simple-directive.tex > out.txt 2>&1 )
    rc=$?
    check "${LEG}: run1 exit code" 0 "${rc}"
    check "${LEG}: run1 success report" 1 "$(count_in SUCCESS "${out}")"
    check "${LEG}: run1 quiet logging" 0 "$(count_in 'kotlin-logging:' "${out}")"
}

run2() {
    local java="$1" out rc
    out="${SMOKE}/run2/out.txt"
    ( cd "${SMOKE}/run2" && "${java}" -jar "${APP_JAR}" --engine lua lua-engine.tex > out.txt 2>&1 )
    rc=$?
    check "${LEG}: run2 exit code" 0 "${rc}"
    check "${LEG}: run2 engine works" 1 "$(count_in LuaEngineWorks "${out}")"
    check "${LEG}: run2 quiet logging" 0 "$(count_in 'kotlin-logging:' "${out}")"
}

run3() {
    local java="$1" out rc
    out="${SMOKE}/run3/out.txt"
    ( cd "${SMOKE}/run3" && "${java}" -jar "${APP_JAR}" --engine lua lua-engine-conditionals.tex > out.txt 2>&1 )
    rc=$?
    check "${LEG}: run3 exit code" 0 "${rc}"
    check "${LEG}: run3 one" 1 "$(count_in One "${out}")"
    check "${LEG}: run3 two" 0 "$(count_in Two "${out}")"
    check "${LEG}: run3 three" 1 "$(count_in Three "${out}")"
    check "${LEG}: run3 four" 1 "$(count_in Four "${out}")"
}

run4() {
    local java="$1" out rc collapsed
    out="${SMOKE}/run4/bad.txt"
    ( cd "${SMOKE}/run4" && "${java}" -jar "${APP_JAR}" --engine lua badtype.tex > bad.txt 2>&1 )
    rc=$?
    collapsed="$(tr '\n' ' ' < "${out}")"
    check "${LEG}: run4 exit code" 2 "${rc}"
    check "${LEG}: run4 typed error detail" 1 "$(printf '%s' "${collapsed}" | grep -c "bad argument #1 to 'exists'")"
}

run5() {
    local java="$1" out rc
    out="${SMOKE}/run5/out.txt"
    ( cd "${SMOKE}/run5" && "${java}" -jar "${APP_JAR}" --engine lua lua-engine-options.tex > out.txt 2>&1 )
    rc=$?
    check "${LEG}: run5 exit code" 0 "${rc}"
    check "${LEG}: run5 echoed basename" 1 "$(count_in 'name: lua-engine-options' "${out}")"
}

tex_db() {
    local java="$1" outa outb rca rcb
    outa="${SMOKE}/tex-db/outa.txt"
    outb="${SMOKE}/tex-db/outb.txt"
    rm -f "${SMOKE}/tex-db/arara.yaml" "${SMOKE}/tex-db/dbcheck.pdf"
    ( cd "${SMOKE}/tex-db" && "${java}" -jar "${APP_JAR}" --verbose --engine lua dbcheck.tex > outa.txt 2>&1 )
    rca=$?
    ( cd "${SMOKE}/tex-db" && "${java}" -jar "${APP_JAR}" --verbose --engine lua dbcheck.tex > outb.txt 2>&1 )
    rcb=$?
    check "${LEG}: tex-db first run exit code" 0 "${rca}"
    check "${LEG}: tex-db first run typesets" 1 "$(count_in 'This is pdfTeX' "${outa}")"
    check "${LEG}: tex-db second run exit code" 0 "${rcb}"
    check "${LEG}: tex-db second run skips" 0 "$(count_in 'This is pdfTeX' "${outb}")"
}

tex_yaml() {
    local java="$1" out rc
    out="${SMOKE}/tex-yaml/out.txt"
    ( cd "${SMOKE}/tex-yaml" && "${java}" -jar "${APP_JAR}" --verbose tex-yaml.tex > out.txt 2>&1 )
    rc=$?
    check "${LEG}: tex-yaml exit code" 0 "${rc}"
    check "${LEG}: tex-yaml typesets" 1 "$(count_in 'This is pdfTeX' "${out}")"
}

tex_lua() {
    local java="$1" out rc
    out="${SMOKE}/tex-lua/out.txt"
    ( cd "${SMOKE}/tex-lua" && "${java}" -jar "${APP_JAR}" --verbose --engine lua tex-lua.tex > out.txt 2>&1 )
    rc=$?
    check "${LEG}: tex-lua exit code" 0 "${rc}"
    check "${LEG}: tex-lua typesets" 1 "$(count_in 'This is pdfTeX' "${out}")"
}

run_matrix() {
    local java="$1"
    run1 "${java}"
    run2 "${java}"
    run3 "${java}"
    run4 "${java}"
    run5 "${java}"
    tex_db "${java}"
    tex_yaml "${java}"
    tex_lua "${java}"
}

find_floor_java() {
    local candidate
    local -a candidates=()
    for candidate in "${HOME}"/.gradle/jdks/*-${FLOOR}-*/bin/java; do
        [[ -x "${candidate}" ]] && candidates+=("${candidate}")
    done
    if [[ -n "${SMOKE_JAVA_HOME:-}" && -x "${SMOKE_JAVA_HOME}/bin/java" ]]; then
        candidates+=("${SMOKE_JAVA_HOME}/bin/java")
    fi
    if command -v java >/dev/null 2>&1; then
        candidates+=("$(command -v java)")
    fi
    for candidate in ${candidates[@]+"${candidates[@]}"}; do
        if [[ "$(java_major "${candidate}")" == "${FLOOR}" ]]; then
            printf '%s' "${candidate}"
            return 0
        fi
    done
    return 1
}

provision_floor_java() {
    say "== no Java ${FLOOR} runtime found; provisioning through the build"
    local resolved
    resolved="$( cd "${ROOT}" && ./gradlew -q printSmokeJavaRuntime --console=plain | tail -1 )"
    if [[ -z "${resolved}" || ! -x "${resolved}" ]]; then
        say "   the runtime task returned no usable path"
        return 1
    fi
    if [[ "$(java_major "${resolved}")" != "${FLOOR}" ]]; then
        say "   the provisioned runtime did not validate"
        return 1
    fi
    FLOOR_JAVA="${resolved}"
}

main() {
    if [[ -z "${FLOOR}" ]]; then
        say "FATAL: the smoke floor key is missing from the properties file"
        exit 1
    fi
    resolve_underlying
    build_jar
    provision_tree

    LEG="underlying JVM"
    say ""
    say "== scenario matrix on the underlying JVM"
    run_matrix "${UNDERLYING_JAVA}"

    if FLOOR_JAVA="$(find_floor_java)"; then
        say "== Java ${FLOOR} runtime found: ${FLOOR_JAVA}"
    elif provision_floor_java; then
        say "== Java ${FLOOR} runtime provisioned: ${FLOOR_JAVA}"
    else
        FLOOR_JAVA=""
        say "== SKIPPED: the Java ${FLOOR} leg — no runtime found and provisioning failed;"
        say "   the artifact was not exercised on the oldest supported floor"
    fi

    if [[ -n "${FLOOR_JAVA}" ]]; then
        LEG="Java ${FLOOR} floor"
        say ""
        say "== scenario matrix on the Java ${FLOOR} floor"
        run_matrix "${FLOOR_JAVA}"
    fi

    say ""
    if [[ "${FAILURES}" -eq 0 ]]; then
        say "SMOKE RESULT: every assertion passed"
        exit 0
    fi
    say "SMOKE RESULT: ${FAILURES} assertion(s) failed"
    for name in ${FAILURE_NAMES[@]+"${FAILURE_NAMES[@]}"}; do
        say "  — ${name}"
    done
    exit 1
}

main "$@"
