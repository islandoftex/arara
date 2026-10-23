{
  description = "arara - the cool TeX automation tool";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    devlab-theme = {
      url = "git+https://codeberg.org/RiPetitor/devlab-theme.git?rev=2b638831313058ba92d17095fce15454fda02eba";
      flake = false;
    };
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    { self
    , flake-utils
    , devlab-theme
    , nixpkgs
    , ...
    }:
    flake-utils.lib.eachSystem [ "x86_64-linux" ] (
      system:
      let
        pkgs = import nixpkgs { inherit system; };
        websiteZola =
          assert pkgs.zola.version == "0.23.6";
          pkgs.zola;
      in
      {
        formatter = pkgs.nixfmt-tree;

        packages =
          let
            # This version corresponds to the major version of arara.
            # If a more granular adjustment is needed, please raise an issue.
            # see also https://gitlab.com/islandoftex/arara/-/issues/97
            version = "7.0.0";
          in
          {
            # The same pinned tool powers local previews and the Nix build.
            website-zola = websiteZola;
            website = pkgs.stdenvNoCC.mkDerivation {
              inherit version;
              pname = "arara-website";

              nativeBuildInputs = [
                pkgs.kotlin
                pkgs.jdk21
                websiteZola
              ];

              src = ./.;
              # Tera load_data initializes a TLS client even for local files.
              SSL_CERT_FILE = "${pkgs.cacert}/etc/ssl/certs/ca-bundle.crt";
              JAVA_HOME = pkgs.jdk21.home;

              buildPhase = ''
                runHook preBuild
                kotlinc buildSrc/src/main/kotlin/org/islandoftex/arara/build/website/*.kt \
                  -include-runtime -d "$TMPDIR/website-tools.jar"
                java -cp "$TMPDIR/website-tools.jar" org.islandoftex.arara.build.website.WebsiteToolKt \
                  prepare "$PWD" "$TMPDIR/site" ${devlab-theme}
                zola --root "$TMPDIR/site" build --output-dir "$TMPDIR/public"
                runHook postBuild
              '';

              doCheck = true;
              checkPhase = ''
                runHook preCheck
                kotlinc buildSrc/src/test/kotlin/org/islandoftex/arara/build/website/WebsiteToolsChecks.kt \
                  -classpath "$TMPDIR/website-tools.jar" -d "$TMPDIR/website-tests.jar"
                java -cp "$TMPDIR/website-tools.jar:$TMPDIR/website-tests.jar" \
                  org.islandoftex.arara.build.website.WebsiteToolsChecksKt "$PWD" ${devlab-theme}
                java -cp "$TMPDIR/website-tools.jar" org.islandoftex.arara.build.website.WebsiteToolKt \
                  check "$TMPDIR/public"
                runHook postCheck
              '';

              installPhase = ''
                mkdir -p $out/public
                cp -r "$TMPDIR/public/." $out/public/
              '';
            };
          };

        devShells.texIntegration = self.devShells.${system}.default.overrideAttrs (old: {
          nativeBuildInputs = old.nativeBuildInputs ++ [ pkgs.texliveSmall ];
        });

        devShells.default = pkgs.mkShell {
          JAVA_HOME = pkgs.corretto21.home;
          SSL_CERT_FILE = "${pkgs.cacert}/etc/ssl/certs/ca-bundle.crt";
          # Generic Linux toolchains downloaded by Foojay do not run in the Nix image.
          shellHook = ''
            export GRADLE_OPTS="''${GRADLE_OPTS:-} -Dorg.gradle.project.org.gradle.java.installations.paths=${pkgs.corretto21.home},${pkgs.jdk25.home} -Dorg.gradle.project.org.gradle.java.installations.auto-detect=false -Dorg.gradle.project.org.gradle.java.installations.auto-download=false"
          '';
          buildInputs = with pkgs; [
            corretto21
            jdk25
            nixpkgs-fmt
            python3Packages.weasyprint
            zip
            websiteZola
          ];
        };
      }
    );
}
