{
  description = "Java 21 + JavaFX development environment for A* Maze MVP";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs = { self, nixpkgs }:
    let
      systems = [
        "x86_64-linux"
        "aarch64-linux"
      ];

      forAllSystems = nixpkgs.lib.genAttrs systems;
    in {
      devShells = forAllSystems (system:
        let
          pkgs = import nixpkgs {
            inherit system;
          };

          javafxRuntimeLibs = pkgs.lib.makeLibraryPath [
            # GTK / GLib
            pkgs.gtk3
            pkgs.glib
            pkgs.pango
            pkgs.atk
            pkgs.gdk-pixbuf
            pkgs.cairo

            # Input / keyboard
            pkgs.libxkbcommon

            # OpenGL
            pkgs.libGL
            pkgs.mesa

            # X11
            pkgs.xorg.libX11
            pkgs.xorg.libXext
            pkgs.xorg.libXrender
            pkgs.xorg.libXi
            pkgs.xorg.libXtst
            pkgs.xorg.libXfixes
            pkgs.xorg.libXrandr
          ];
        in {
          default = pkgs.mkShell {
            packages = with pkgs; [
              jdk21
              maven

              gtk3
              glib
              pango
              atk
              gdk-pixbuf
              cairo
              libxkbcommon
              libGL
              mesa
            ];

            JAVA_HOME = "${pkgs.jdk21}";

            LD_LIBRARY_PATH = javafxRuntimeLibs;

            shellHook = ''
              echo ""
              echo "Java:"
              java -version
              echo ""
              echo "Maven:"
              mvn -version
              echo ""
              echo "LD_LIBRARY_PATH:"
              echo "$LD_LIBRARY_PATH"
              echo ""
              echo "Execute:"
              echo "  mvn javafx:run"
              echo ""
            '';
          };
        });
    };
}
