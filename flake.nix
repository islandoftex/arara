{
  description = "arara - the cool TeX automation tool";

  inputs = {
    nixpkgs.url = "nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      flake-utils,
      nixpkgs,
      ...
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
          buildInputs = with pkgs; [
            corretto21
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
