{
  description = "arara - the cool TeX automation tool";

  inputs = {
    nixpkgs.url = "nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
    gradle2nix.url = "github:tadfisher/gradle2nix/v2";
    gradle2nix.inputs = {
      nixpkgs.follows = "nixpkgs";
      flake-utils.follows = "flake-utils";
    };
  };

  outputs =
    {
      self,
      flake-utils,
      nixpkgs,
      gradle2nix,
    }:
    flake-utils.lib.eachSystem [ "x86_64-linux" ] (
      system:
      let
        pkgs = import nixpkgs { inherit system; };
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
            default = gradle2nix.builders.${system}.buildGradlePackage {
              inherit version;
              pname = "arara";

              src = ./.;

              # generated using `gradle2nix` (no arguments required)
              lockFile = ./gradle.lock;

              nativeBuildInputs = [ pkgs.makeWrapper ];

              gradleBuildFlags = [
                "--no-daemon"
                "assembleDist"
              ];

              installPhase = ''
                mkdir -p $out/share/java/

                install -Dm644 cli/build/libs/arara-cli-with-deps-*.jar $out/share/java/arara-${version}.jar
                install -Dm644 -t $out/share/java/rules rules/*

                makeWrapper ${pkgs.jre}/bin/java $out/bin/arara \
                  --add-flags "-jar $out/share/java/arara-${version}.jar"
              '';

              meta = with pkgs.lib; {
                homepage = "https://gitlab.com/islandoftex/arara";
                description = "arara is a TeX automation tool based on rules and directives. It gives you a way to enhance your TeX experience.";
                license = licenses.bsd3;
              };
            };
            website =
              let
                juiceTheme = pkgs.fetchFromGitHub {
                  owner = "huhu";
                  repo = "juice";
                  rev = "0dc909d59066a0c8cc9f7bcaa6f6853e7574452d";
                  hash = "sha256-WtnU0CA4yf9Cad07uvIy+ma/nlrkNChUnWY38fvyZq8=";
                };
              in
              pkgs.stdenvNoCC.mkDerivation {
                inherit version;
                pname = "arara-website";

                nativeBuildInputs = [
                  pkgs.gnused
                  pkgs.zola
                ];

                src = ./.;

                # TODO: set updated date based on git if date != updated for all posts
                buildPhase = ''
                  tail -n +8 README.md | sed 's/## /# /g' >> website/content/_index.md
                  tail -n +3 CHANGELOG.md | sed 's/## /# /g' >> website/content/CHANGELOG.md

                  pushd website
                  mkdir -p themes/juice
                  cp -rf ${juiceTheme}/{theme.toml,sass,static,templates} themes/juice/
                  zola build
                  popd
                '';

                installPhase = ''
                  mkdir -p $out/public
                  cp -r ./website/public/. $out/public/
                '';
              };
          };

        devShells.default = pkgs.mkShell {
          inputsFrom = [ self.packages.${system}.default ];
          buildInputs = with pkgs; [
            gradle2nix.packages.${system}.gradle2nix
            corretto11
            htmlq
            nixpkgs-fmt
            python3Packages.weasyprint
            zip
            zola
          ];
        };
      }
    );
}
