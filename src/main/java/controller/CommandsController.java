package controller;

import model.Config;
import model.LogMessages;
import org.apache.log4j.Logger;
import util.JDBCConnection;
import model.Messages;
import model.ReplyCode;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class CommandsController {

    private JDBCConnection connection;
    private String username;
    public static String ROOT = "/etc/ftRoot";
    private String SPACE = " ";
    private String COMMA = ",";
    private String dataType = "I";
    private int SIZE_OF_COMMAND_WITHOUT_ARGUMENT = 1;
    private int SIZE_OF_COMMAND_WITH_ONE_ARGUMENT = 2;
    private int SIZE_OF_COMMAND_WITH_TWO_ARGUMENT = 3;
    private int FIRST_ARGUMENT_INDEX = 1;

    private static Logger log = Logger.getLogger(CommandsController.class);

    public CommandsController() {
        connection = new JDBCConnection();
    }

    public String userCommand(String message) {
        String[] messageSplit = message.split(SPACE);

        if (messageSplit.length == SIZE_OF_COMMAND_WITH_ONE_ARGUMENT) {

            username = messageSplit[FIRST_ARGUMENT_INDEX];
            boolean user = connection.getUsername(username);
            System.out.println(user);
            if (user) {
                log.info(LogMessages.USERNAME_OK_MESSAGE);
                return ReplyCode.CODE_331;
            } else {
                log.info(LogMessages.NO_SUCH_USER_MESSAGE);
                return ReplyCode.CODE_530;
            }

        } else {
            log.info(LogMessages.WRONG_COMMAND_MESSAGE);
            return ReplyCode.CODE_500;
        }

    }

    public String passwordCommand(String message) {
        String[] messageSplit = message.split(SPACE);

        if (messageSplit.length ==  SIZE_OF_COMMAND_WITH_ONE_ARGUMENT) {

            String password  = messageSplit[FIRST_ARGUMENT_INDEX];
            boolean user = connection.getUser(username, password);

            if (user) {
                log.info(LogMessages.PASSWORD_OK_MESSAGE);
                return ReplyCode.CODE_230;
            } else {
                log.info(LogMessages.WRONG_PASSWORD_MESSAGE);
                return ReplyCode.CODE_530;
            }

        } else {
            log.info(LogMessages.WRONG_COMMAND_MESSAGE);
            return ReplyCode.CODE_500;
        }
    }


    public String systCommand(String message) {
        String[] messageSplit = message.split(SPACE);

        if (messageSplit.length ==  SIZE_OF_COMMAND_WITHOUT_ARGUMENT) {
            log.info(LogMessages.SYSTEM_TYPE_MESSAGE );
            return ReplyCode.CODE_215;
        } else {
            log.info(LogMessages.WRONG_COMMAND_MESSAGE);
            return ReplyCode.CODE_500;
        }
    }

    public String printWorkDirCommand(String message){
        String[] messageSplit = message.split(SPACE);

        if (messageSplit.length == SIZE_OF_COMMAND_WITHOUT_ARGUMENT) {
            log.info(LogMessages.PRINT_ROOT_DIRECTORY_MESSAGE + ROOT);
            return ReplyCode.CODE_257;
        } else {
            log.info(LogMessages.WRONG_COMMAND_MESSAGE);
            return ReplyCode.CODE_500;
        }
    }

    public String pasvResponse(String message) {
        String[] messageSplit = message.split(SPACE);

        if (messageSplit.length == SIZE_OF_COMMAND_WITHOUT_ARGUMENT) {
            log.info(LogMessages.SENT_ADDRESS_MESSAGE);
            return ReplyCode.CODE_227;
        } else {
            log.info(LogMessages.WRONG_COMMAND_MESSAGE);
            return ReplyCode.CODE_500;
        }

    }

    public String getType(String message) {
        String[] messageSplit = message.split(SPACE);

        if (messageSplit.length == SIZE_OF_COMMAND_WITH_ONE_ARGUMENT) {
            dataType = messageSplit[1];

            if (dataType.equals(Config.TYPE_A)) {
                log.info(LogMessages.TYPE_OF_DATA_IS_A_MESSAGE);
                return ReplyCode.CODE_200;
            } else if (dataType.equals(Config.TYPE_I)) {
                log.info(LogMessages.TYPE_OF_DATA_IS_I_MESSAGE);
                return ReplyCode.CODE_200;
            } else {
                log.info(LogMessages.WRONG_COMMAND_MESSAGE);
                return ReplyCode.CODE_501;
            }
        }
        else {
            log.info(LogMessages.WRONG_COMMAND_MESSAGE);
            return ReplyCode.CODE_501;
        }

    }


    public String portCommand(String message) {
        String[] messageSplit = message.split(SPACE);

        if (messageSplit.length == SIZE_OF_COMMAND_WITH_ONE_ARGUMENT) {
            String[] hostAndPorts = messageSplit[1].split(COMMA);
            String host = hostAndPorts[0] +"."+hostAndPorts[1] +"."+ hostAndPorts[2] + "."+hostAndPorts[3];
            int port = Integer.parseInt(hostAndPorts[4])* Config.BIT_SHIFT + Integer.parseInt(hostAndPorts[5]);

            try {
                new ServerSocket(port, Config.MAX_CONNECTION_NUMBER,  InetAddress.getByName(host));
                log.info(LogMessages.PORT_COMMAND_MESSAGE);
                return ReplyCode.CODE_200;
            } catch (IOException e) {
                log.info(LogMessages.PORT_COMMAND_MESSAGE);
                e.printStackTrace();
                return ReplyCode.CODE_200;//КАКОЙ ТО ОШИБКА ЗДЕСЬ ДОЛЖЕН БЫТЬ
            }
        } else return ReplyCode.CODE_501;
    }





    public String removeDirectoryCommand(String message) {
        String[] messageSplit = message.split(SPACE);

        if (messageSplit.length == SIZE_OF_COMMAND_WITH_ONE_ARGUMENT) {
            File file = new File(ROOT + File.pathSeparator + messageSplit[FIRST_ARGUMENT_INDEX]);

            if (file.isDirectory() && file.delete()) {
                return ReplyCode.CODE_200;
            } else {
                return "Error"; // либо не удалена либо не директория
            }

        } else {
            return ReplyCode.CODE_500;
        }
    }


    public String deleteCommand(String message) {

        String[] messageSplit = message.split(SPACE);

        if (messageSplit.length == SIZE_OF_COMMAND_WITH_ONE_ARGUMENT) {
            File file = new File(ROOT + File.pathSeparator + messageSplit[FIRST_ARGUMENT_INDEX]);

            if (!file.isDirectory() && file.delete()) {
                return ReplyCode.CODE_200;
            } else {
                return "Error"; // либо не удалена либо не файл
            }
        } else {
            return ReplyCode.CODE_500;
        }

    }

    public String makeDirectoryCommand(String message) {

        String[] messageSplit = message.split(SPACE);

        if (messageSplit.length == SIZE_OF_COMMAND_WITH_ONE_ARGUMENT) {
            File file = new File(ROOT + File.pathSeparator + messageSplit[FIRST_ARGUMENT_INDEX]);

            if (file.mkdir()) {
                return ReplyCode.CODE_200;
            } else {
                return "Error"; //не создали
            }
        } else {
            return ReplyCode.CODE_500;
        }
    }

    public String listCommand(String message, Messages answer) throws IOException {
        String[] messageSplit = message.split(SPACE);

        if (messageSplit.length == SIZE_OF_COMMAND_WITHOUT_ARGUMENT) {
            File root = new File(ROOT);

            List files = new ArrayList<String>();
            List dirs = new  ArrayList<String>();

            if(root.listFiles() != null){
                for(File file: root.listFiles()){

                    if(!file.isDirectory()) files.add(file.getName());
                    else  dirs.add(file.getName());

                }
            } else return ReplyCode.CODE_500;

//            answer.printFileList(files);
            answer.printDirectoryList(dirs, files);

            return ReplyCode.CODE_200;

        } else return ReplyCode.CODE_500;
    }

    public String changeWorkDirCommand(String message){
        String[] messageSplit = message.split(SPACE);

        if (messageSplit.length == SIZE_OF_COMMAND_WITH_ONE_ARGUMENT) {
            File rootNew = new File(messageSplit[FIRST_ARGUMENT_INDEX]);

            if(rootNew.isDirectory() && rootNew.exists()){
                ROOT = messageSplit[FIRST_ARGUMENT_INDEX];
                return ReplyCode.CODE_200;

            } else return ReplyCode.CODE_200;// либо нет директории либо не лиректория

        } else return ReplyCode.CODE_500;
    }




    public String retrieveCommand(String message) {
        String[] messageSplit = message.split(" ");

        return "Error";
    }


    public String storeCommand(String message) {
        String[] messageSplit = message.split(" ");

        return "Error";
    }








}
