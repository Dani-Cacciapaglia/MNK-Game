javac -d build myplayer/*.java MNKGAME2.0/*.java
cd build
java mnkgame.MNKPlayerTester 3 3 3 mnkgame.QuasiRandomPlayer Player.Player -v -t 10 -r 1
PAUSE 