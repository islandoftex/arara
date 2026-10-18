{
  description = "arara - the cool TeX automation tool";

  inputs = {
    nixpkgs.url = "nixpkgs/nixos-unstable";
    devlab-theme = {
      url = "git+https://codeberg.org/RiPetitor/devlab-theme.git?rev=2b638831313058ba92d17095fce15454fda02eba";
      flake = false;
    };
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      flake-utils,
      devlab-theme,
      nixpkgs,
      ...
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
                pkgs.python3
                websiteZola
              ];

              src = ./.;
              # Tera load_data initializes a TLS client even for local files.
              SSL_CERT_FILE = "${pkgs.cacert}/etc/ssl/certs/ca-bundle.crt";

              buildPhase = ''
                runHook preBuild
                python3 scripts/prepare-website.py --output "$TMPDIR/site" --theme ${devlab-theme}
                zola --root "$TMPDIR/site" build --output-dir "$TMPDIR/public"
                runHook postBuild
              '';

              installPhase = ''
                mkdir -p $out/public
                cp -r "$TMPDIR/public/." $out/public/
              '';
            };
          };

        devShells.default = pkgs.mkShell {
          JAVA_HOME = pkgs.corretto21.home;
          # Generic Linux toolchains downloaded by Foojay do not run in the Nix image.
          shellHook = ''
            export GRADLE_OPTS="''${GRADLE_OPTS:-} -Dorg.gradle.project.org.gradle.java.installations.paths=${pkgs.corretto21.home},${pkgs.jdk11.home} -Dorg.gradle.project.org.gradle.java.installations.auto-detect=false -Dorg.gradle.project.org.gradle.java.installations.auto-download=false"
          '';
          buildInputs = with pkgs; [
            corretto21
            jdk11
            htmlq
            nixpkgs-fmt
            python3Packages.weasyprint
            zip
            websiteZola
          ];
        };
      }
    );
}
