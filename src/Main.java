/*
        프로그램 개요 :

        마우스 움직임을 기준으로, 컴퓨터를 장시간 이용하지 않으면 알림음을 출력하는 간단한 프로그램
        기본 기능 구현 후 사용자가 자유롭게 사운드를 등록하여 사용할 수 있도록 GUI 제작 예정
        exe 배포를 염두로 하여 JAVA17보다 JAVA11을 채택

        개발 기간 : 2022.12.26 ~ 2023.01.??
        개발자 : 이지현
        사양 : JAVA SDK - Zulu 11
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import jaco.mp3.player.MP3Player;
import java.io.File;

public class Main {

    // JACo MP3 Player 이용하여 소리 재생용 Class
    static class PlayMP3 extends Thread {

        List<File> fileList;
        MP3Player mp3Player;

        public PlayMP3(List<File> fileList) {
            this.fileList = fileList;
        }

        public void run() {
            try {
                int listsize = fileList.size();
                int theIndex = (int) ((Math.random()*10000)%listsize);
                mp3Player = new MP3Player(fileList.get(theIndex));
                mp3Player.play();

                while(!mp3Player.isStopped()) {
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public static void makeTray() {
        MenuItem exititem = new MenuItem("exit");
        PopupMenu menu = new PopupMenu("My Menu");

        menu.add(exititem);
        exititem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(1);
            }
        });

        TrayIcon myTray =
                new TrayIcon(Toolkit.getDefaultToolkit().getImage("src/lib/alarmpuppyicon.png"), "Timer Alarm Project", menu);
        SystemTray tray = SystemTray.getSystemTray();

        try {
            tray.add(myTray);
        } catch (AWTException e1) {
            System.out.println(e1.getMessage());
        }
        myTray.setImageAutoSize(true);
    }

    public static void main(String[] args) throws InterruptedException {

        System.out.println("Timer Alarm Started!");
        makeTray();

        PointerInfo pt = null;
        PlayMP3 playMP3 = null;

        // 사용자가 지정한 디렉토리에서 mp3 파일을 fileList에 저장
        // String DATA_DIRECTORY = "C:\\Users\\ooppy\\Downloads\\alram";
        String DATA_DIRECTORY = System.getProperty("user.dir");
        File dir = new File(DATA_DIRECTORY);
        List<File> fileList = new ArrayList<>();

        String[] filenames = dir.list();
        for(String filename : filenames){
            if(filename.contains(".mp3")) {
                System.out.println(DATA_DIRECTORY + "\\" + filename + " is added!");
                fileList.add(new File(DATA_DIRECTORY + "\\" + filename));
            }
        }


        // 비활성 시간이 x초를 경과하면 사용자 사운드 랜덤 재생
        // 재생 후 비활성 시간 0으로 초기화
        int inactiveSec = 0;
        int inactiveLimit = 240;
        double mouse_x = 0, mouse_y = 0;
        double cur_x = 0, cur_y = 0;

        while(true){
            // 커서 좌표 받기
            pt = MouseInfo.getPointerInfo();
            cur_x = pt.getLocation().getX();
            cur_y = pt.getLocation().getY();

            // 마우스 제자리에 있음
            if(mouse_x == cur_x && mouse_y == cur_y) {
                inactiveSec++;
                System.out.println(inactiveSec+" sec inactived...");

                if(inactiveSec >= inactiveLimit) {
                    // fileList의 랜덤 소리 재생
                    playMP3 = new PlayMP3(fileList);
                    playMP3.setDaemon(true);
                    playMP3.start();
                    System.out.println("Alarm Played!");

                    inactiveSec = 0;
                }
            }
            // 마우스 이동함
            else {
                mouse_x = cur_x;
                mouse_y = cur_y;

                inactiveSec = 0;
            }

            Thread.sleep(1000);
        }

    }
}