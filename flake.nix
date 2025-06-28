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

  outputs = { self, flake-utils, nixpkgs, gradle2nix }:
    flake-utils.lib.eachSystem [ "x86_64-linux" ] (system:
      let
        pkgs = import nixpkgs { inherit system; };
      in
      {
        defaultPackage = gradle2nix.builders.${system}.buildGradlePackage rec {
          pname = "arara";
          # This version corresponds to the major version of arara.
          # If a more granular adjustment is needed, please raise an issue.
          # see also https://gitlab.com/islandoftex/arara/-/issues/97
          version = "7.0.0";

          src = ./.;

          # generated using `gradle2nix` (no arguments required)
          lockFile = ./gradle.lock;

          nativeBuildInputs = [ pkgs.makeWrapper ];

          gradleBuildFlags = [ "--no-daemon" ":cli:shadowJar" ];

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

        devShell = pkgs.mkShell {
          inputsFrom = builtins.attrValues self.defaultPackage;
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
