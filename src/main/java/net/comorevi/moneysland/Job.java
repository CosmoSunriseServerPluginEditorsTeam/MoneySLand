package net.comorevi.moneysland;

import java.util.HashMap;
import java.util.LinkedHashMap;

import cn.nukkit.Player;
import net.comorevi.moneyapi.MoneySAPI;

public class Job {

    public static final int WAITING  = 0;
    public static final int IN_ENTRY = 1;
    public static final int CHECKING = 2;
    public static final int BOUGHT   = 3;

    public static final int JOB_SUCCESSFUL   = 0;
    public static final int JOB_ERROR        = 1;


    public static final int ERROR_INVALID_VALUE   = 0;
    public static final int ERROR_NO_MONEY        = 1;
    public static final int ERROR_ALREADY_USED    = 2;
    public static final int ERROR_SIZE_LIMIT_OVER = 3;


    // Singleton
    private static HashMap<String, Job> Job_data = new LinkedHashMap<String, Job>();

    public static Job create(Player player) {
        Job job = new Job(player);
        Job_data.put(player.getName(), job);
        return job;
    }

    public static Job get(Player player) {
        return Job_data.get(player.getName());
    }

    // Objective
    private int status;
    private Player player;

    private int[] start = new int[2]; //[0 => x, 1 => z]
    private int[] end   = new int[2];   //[0 => x, 1 => z]
    private String world;

    public int error = -1;

    private Job(Player player) {
        status = Job.WAITING;
        this.player = player;
    }

    public void start(int x, int z) {
        start[0] = x;
        start[1] = z;
        world = player.getLevel().getFolderName();
        status = Job.IN_ENTRY;
    }

    public void end(int x, int z) {
        end[0] = x;
        end[1] = z;
        world = player.getLevel().getFolderName();
        status = Job.IN_ENTRY;
    }

    public int buy() {
        MoneySLand main = MoneySLand.getInstance();

        int[] pos1 = getStart();
        int[] pos2 = getEnd();
        
        System.out.println(pos1[0] + "::" + pos1[1] + "::" + pos2[0] + "::" + pos2[1]);

        start[0] = Math.min(pos1[0], pos2[0]); // x minimum
        start[1] = Math.min(pos1[1], pos2[1]); // z minimum
        end[0]   = Math.max(pos1[0], pos2[0]); // x maximum
        end[1]   = Math.max(pos1[1], pos2[1]); // z maximum
        
        System.out.println(start[0] + "::" + start[1] + "::" + end[0] + "::" + end[1]);

        if(!isValidValue()) {
            error = Job.ERROR_INVALID_VALUE;
            return Job.JOB_ERROR;
        }

        int price = main.calculateLandPrice(player);
        int size  = main.calculateLandSize(player);

        if (MoneySAPI.getInstance().getMoney(player) < price) {
            error = Job.ERROR_NO_MONEY;
            return Job.JOB_ERROR;
        }

        if(main.checkOverLap(start, end, world)) {
            error = Job.ERROR_ALREADY_USED;
            return Job.JOB_ERROR;
        }

        if(size >= MoneySLand.maxLandSize) {
            error = Job.ERROR_SIZE_LIMIT_OVER;
            return Job.JOB_ERROR;
        }

        MoneySAPI.getInstance().addMoney(player, -price);
        main.createLand(player.getName(), start, end, size, world);

        status = BOUGHT;
        return Job.JOB_SUCCESSFUL;
    }

    public boolean isValidValue() {
        if (!((start[0] == 0 && start[1] == 0) && (end[0] == 0 && end[1] == 0))) { //初期値ではないか（初期値ではない、有効な値であれば真）
            return true;
        } else {
            return false;
        }
    }

    public int[] getStart() {
        return start;
    }

    public int[] getEnd() {
        return end;
    }

    public int getStatus() {
        return status;
    }

    public String getErrorMessage() {
        MoneySLand main = MoneySLand.getInstance();
        String message = "";
        // TODO エラーごとにメッセージを指定
        switch (error) {
            case ERROR_INVALID_VALUE:
                message = main.translateString("error-not-selected");
                return message;

            case ERROR_NO_MONEY:
                message = main.translateString("error-no-money");
                return message;

            case ERROR_ALREADY_USED:
                message = main.translateString("error-land-alreadyused");
                return message;

            case ERROR_SIZE_LIMIT_OVER: //特別なやつなのでここで処理
                message = main.translateString("error-landSizeLimitOver", String.valueOf(main.calculateLandSize(player)), String.valueOf(MoneySLand.maxLandSize));
                return message;

            default:
                message = main.translateString("error-notFoundKey");
                return message;
        }
    }

}
