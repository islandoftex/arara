{
  description = "arara - the cool TeX automation tool";

  inputs = {
    nixpkgs.url = "nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, flake-utils, nixpkgs }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
      in
      {
        defaultPackage =
          with import nixpkgs { inherit system; };
          pkgs.stdenv.mkDerivation rec {
            pname = "arara";
            version = "6.1.7";

            src = ./.;

            nativeBuildInputs = [ pkgs.jdk8 ];

            buildPhase = ''
              export GRADLE_USER_HOME=$PWD
              ./gradlew --no-daemon :cli:shadowJar
            '';

            installPhase = ''
              mkdir -p $out/bin
              cp cli/build/libs/arara-cli-with-deps*.jar $out/bin/arara.jar
            '';

            meta = with pkgs.lib; {
              homepage = "https://gitlab.com/islandoftex/arara";
              description = "arara is a TeX automation tool based on rules and directives. It gives you a way to enhance your TeX experience.";
              license = licenses.bsd3;
            };
          };

        devShell = pkgs.mkShell {
          inputsFrom = builtins.attrValues self.defaultPackage;
          buildInputs = [ pkgs.zola pkgs.nixpkgs-fmt ];
        };
      }
    );
}