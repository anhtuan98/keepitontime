package vn.eagleeye.keepitontime;

/**
 * Created by natuan on 11/16/16.
 */

public class NetworkCommands
{
    public static String writeClientIdCommand(int id)
    {
        String result = "YourID="+ id;
        return result;
    }
    public int readClientIdCommand(String command)
    {
        String strId = command.substring(command.indexOf('='));

        int id =-1;
        try
        {
            id = Integer.parseInt(strId);
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return id;
    }



    public static String writePlayMediaCommand(String fileName)
    {
        String result = "PlayMedia="+ fileName;
        return result;
    }

    public static String writePlayMediaFromNetworkCommand(String fileName)
    {
        String result = "PlayMediaNetwork="+ fileName;
        return result;
    }

    public static String writePlayMediaFromUrlCommand(String url)
    {
        String result = "PlayMediaUrl="+ url;
        return result;
    }
    public static String writeDownloadAndPlayMediaFromUrlCommand(String url)
    {
        String result = "DownloadPlayMediaUrl="+ url;
        return result;
    }
    public static String writeMountNetworkFolder(String networkAddress)
    {
        String result = "MountNetwork="+ networkAddress;
        return result;
    }

    public static String writeMountProtectedNetworkFolder(String networkAddress,String user, String password)
    {
        String result = "MountProtectedNetwork="+ networkAddress+",User="+user+",Pass="+password;
        return result;
    }

    public static String writeSendNetworkFolderInfo(String networkAddress)
    {
        String result = "SendNetworkInfo="+ networkAddress;
        return result;
    }


}
