all:
	javac MultiClientServer.java
	javac TitleScreen.java Login.java CharacterSelect.java MainScreen.java ClientMain.java
clean:
	rm -f *.class