all:
	javac MultiClientServer.java
	javac MOMIJ.java Login.java CharacterSelect.java MainScreen.java ClientMain.java
clean:
	rm -f *.class