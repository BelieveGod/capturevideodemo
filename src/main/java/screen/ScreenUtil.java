package screen;

import javax.imageio.ImageIO;
import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/5/12 14:44
 */
public class ScreenUtil {
    static String ffmpeg = ScreenUtil.class.getResource("ffmpeg.exe").getFile();
    int bigNum = 1000000000;

    private String PATH=null;
    private String fileTypeName = "jpg";
    private String fileType="."+fileTypeName;
    private String fieldName = "test";

    public ScreenUtil(String fieldName){
        this.fieldName = fieldName;
    }

    public void setPATH(String PATH){
        File file = new File(PATH + File.separator + this.fieldName);
        while(file.exists()){
            this.fieldName += "1";
            file=new File(PATH + File.separator + this.fieldName);
        }
        this.PATH = PATH + File.separator;
    }

    public String getPATH() {
        return PATH;
    }


    public String getFileNamePatten(){
        return PATH + fieldName + "/1%09d" + fileType ;
    }

    // 画鼠标
    private void buildMousePic(BufferedImage image,double x,double y){
        Point p = MouseInfo.getPointerInfo().getLocation();
        image.createGraphics().drawOval(p.x - (int)x - 20, p.y - (int)y - 20, 20, 20);
    }

    public void printScreen(double x,double y,double w,double h,int num) throws AWTException, IOException {
        // 转化视频的时候需要时32的倍数和2的倍数
        if(w%32!=0){
            w=w/32*32d;
        }

        if(h%2!=0){
            h=h/2*2d;
        }

        // 截取屏幕
        BufferedImage image =
                new Robot().createScreenCapture(new Rectangle((int) x, (int) y, (int) w, (int) h));
        buildMousePic(image,x,y);

        // 创建一个用于保存图片的文件夹
        File screenCaptureDirectory = new File(PATH + fieldName);
        if (!screenCaptureDirectory.exists()) {
            screenCaptureDirectory.mkdirs();
            System.out.println("The directory"+screenCaptureDirectory.getName()+"is created");
        }

        File imageFile = new File(screenCaptureDirectory, bigNum + num + fileType);
        ImageIO.write(image, fileTypeName, imageFile);
    }

    public void deleteFiled(){
        System.out.println(PATH + fieldName);
        File file = new File(PATH + fieldName);
        deleteFile(file);
    }

    /**
     * 递归删除文件
     * @param file
     */
    private void deleteFile(File file) {
        if(file.isDirectory() && file.listFiles().length>0){
            for (File f : file.listFiles()) {
                deleteFile(f);
            }
            file.delete();
        }else{
            file.delete();
        }
    }

    // 默认帧率
    int framerate=60;

    public void toGif() throws IOException, InterruptedException {
        // 转视频
        String outputMp4 = PATH + fieldName + File.separator + "temp.mp4";
        ProcessBuilder processBuilder =
                new ProcessBuilder(ffmpeg, "-f", "image2", "-framerate", framerate + "", "-i", getFileNamePatten(),
                                   "-vcodec", "libx264", "-r", framerate + "", outputMp4);
        doProcessBuilder(processBuilder);

        // mp4 转gif
        String outputGif=PATH+fieldName+".gif";
        processBuilder=new ProcessBuilder(ffmpeg, "-i", outputMp4, "-r" ,framerate + "",outputGif);
        doProcessBuilder(processBuilder);

        // 执行完删除截图文件，仅保留gif文件
        deleteFiled();

        System.out.println("over ");
        try {
            Desktop.getDesktop().open(new File(PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doProcessBuilder(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        Process process = processBuilder.start();
        BufferedReader readerInput = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String inputLine = "";
        while((inputLine=readerInput.readLine())!=null){
            System.out.println(inputLine);
        }
        process.waitFor();
        process.destroy();
    }

}
