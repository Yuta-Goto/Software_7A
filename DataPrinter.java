class DataPrinter extends Thread {
    private Avatar avatar;
    private boolean running;
    private Person samplePlayer;

    DataPrinter(Avatar avatar) {
        this.avatar = avatar;
        running = true;
    }

    public void run() {
        int i = 0;
        while (running) {
            try {
                // 例として、アバターにランダムな方向に動かすメソッドを呼び出す
                //System.out.println(avatar.GetData());
                samplePlayer = new Person("Tom", 1, 0);
                samplePlayer.SetPersonState(1000, i, 0, 0, "Hello");
                MainScreen.updateRoomMember(samplePlayer);
                Thread.sleep(5); // 1秒ごとに実行
                i++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopRunning() {
        running = false;
    }
}