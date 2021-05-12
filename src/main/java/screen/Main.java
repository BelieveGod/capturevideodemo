package screen;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.*;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/5/12 14:43
 */
public class Main extends Application {

    /**
     * 屏幕尺寸
     */
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private int width = ((int) screenSize.getWidth());
    private int height = ((int) screenSize.getHeight());

    /**
     * 屏幕中心开始，画一个默认的正方形
     */
    private int defaultWidth=100;

    /**
     * 默认线条宽度
     */
    private int defaultLineWidth = 10;

    /**
     * 当点击的时候，给个误差值，确保能达到拖拽目的
     *
     */

    private int defaultFixWidth=10;

    /**
     * 定义
     * 左上角坐标(x,y)
     * 右下角坐标(w,h)
     */
    double x,y,w,h=0d;

    /**
     *    a
     *  d # b
     *    c
     *    定义矩形的四个边，被点击后为true
     */
    boolean a,b,c, d = false;

    boolean stop=true;
    private Stage stage;
    private ExecutorService executorService = newFixedThreadPool(1);
    ScreenUtil screenUtil = new ScreenUtil("random");

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        StackPane root=new StackPane();
        Canvas canvas = new Canvas((width), height);
        Button startBtn = new Button("start");
        Button exitBtn = new Button("exit");
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.RED);
        gc.setLineWidth(defaultLineWidth);
        x = width / 2 - defaultWidth;
        y = height / 2 - defaultWidth;
        w = width / 2 + defaultWidth;
        h = height / 2 + defaultWidth;
        drawRect(gc, x, y, w, h);

        // 拖拽事件
        canvas.setOnMouseDragged(event -> {
            gc.clearRect(0, 0, width, height);
            if(a||c){
                // 上边被点击
                if(d){
                    // 左边被点击（左上角）
                    x = event.getX();
                }else if(b){
                    // 右边被点击（右上角）
                    w=event.getX();
                }
                if(a){
                    y=event.getY();
                }else{
                    h = event.getY();
                }
            }else if(b||d){
                // 右边被点击
                if(a){
                    // 右上角
                    y = event.getY();
                }else if(c){
                    // 右下角
                    h = event.getY();
                }
                if(b){
                    w = event.getX();
                }else{
                    x = event.getX();
                }
            }
            drawRect(gc, x, y, w, h);
            startBtn.setTranslateX(w - width / 2 + startBtn.getWidth() / 2);
            startBtn.setTranslateY(y - height / 2 + startBtn.getHeight() / 2);
            exitBtn.setTranslateX(w - width / 2 + startBtn.getWidth() / 2);
            exitBtn.setTranslateY(y - height / 2 + startBtn.getHeight() / 2+35);
        });

        // 当被点击的时候，取得点击的abcd哪些边
        canvas.setOnMousePressed(event -> {
            a=b=c=d=false;
            // 点击上边界
            if(event.getY()<(y+defaultFixWidth)){
                a=true;
            }
            // 下边界
            if(event.getY()>(h-defaultFixWidth)){
                c=true;
            }
            // 左边界
            if (event.getX() < (x + defaultFixWidth)) {
                d = true;
            }
            // 右边界
            if(event.getX()>(w-defaultFixWidth)){
                b = true;
            }
        });

        root.getChildren().add(canvas);

        // 开始按钮
        root.getChildren().add(startBtn(startBtn));
        // 退出按钮
        root.getChildren().add(exitBtn(exitBtn));

        // 透明底
        root.setBackground(Background.EMPTY);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        Scene scene = new Scene(root, width, height);
        scene.setFill(null);
        primaryStage.setScene(scene);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();

        if(screenUtil.getPATH()==null){
            primaryStage.close();
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("请选择保存路径");
            File directory = directoryChooser.showDialog(new Stage());
            if(directory!=null){
                screenUtil.setPATH(directory.getAbsolutePath());
                primaryStage.show();
            }
        }
    }

    private Node startBtn(Button startBtn) {
        startBtn.setMinWidth(50);
        startBtn.setMinHeight(30);
        startBtn.setTranslateX(w - width / 2 + 30);
        startBtn.setTranslateY(y - height / 2 + 15);
        startBtn.setOnAction(event -> {
            stop = !stop;
            System.out.println("stop:" + stop);
            if(stop){
                startBtn.setText("start");
                dialog();
            }else{
                startBtn.setText("stop");
                printScreenGif();
            }
        });
        return startBtn;
    }

    private Button exitBtn(Button exitBtn){
        exitBtn.setMinWidth(50);
        exitBtn.setMinHeight(30);
        exitBtn.setTranslateX(w-width/2+30);
        exitBtn.setTranslateY(y-height/2+15+35);
        exitBtn.setOnAction(event -> System.exit(0));
        return exitBtn;

    }

    /**
     * todo
     * @param gc
     * @param x
     * @param y
     * @param w
     * @param h
     */
    private void drawRect(GraphicsContext gc, double x, double y, double w, double h) {
        gc.strokeLine(x,y,w,y);
        gc.strokeLine(x,y,x,h);
        gc.strokeLine(w,h,w,y);
        gc.strokeLine(w,h,x,h);

    }
    /**
     * todo
     */
    private void printScreenGif() {
        executorService.execute(() ->{
            try {
                for(int i = 0 ; i < 99999999 ; i++){
                    screenUtil.printScreen(x+defaultLineWidth,y,w-x-defaultLineWidth-defaultLineWidth,h-y-defaultLineWidth-defaultLineWidth,i);
                    Thread.sleep(1000/screenUtil.framerate);
                    if(stop){
                        break;
                    }
                }
                screenUtil.toGif();
                endJob();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    private Stage dialogStage;
    /**
     * todo
     */
    private void dialog() {
        dialogStage = new Stage();
        ProgressIndicator progressIndicator = new ProgressIndicator();
        // 窗口父子关系
        dialogStage.initOwner(stage);
        dialogStage.initStyle(StageStyle.UNDECORATED);
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        // progress bar
        javafx.scene.control.Label label = new Label("操作中....想取消，请杀进程");
        label.setTextFill(Color.RED);
        progressIndicator.setProgress(-1F);
        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setBackground(Background.EMPTY);
        vBox.getChildren().addAll(progressIndicator, label);
        Scene scene = new Scene(vBox);
        scene.setFill(null);
        dialogStage.setScene(scene);
        dialogStage.show();

    }

    /**
     * 收尾工作
     */
    private void endJob() {
        Platform.runLater(() -> {
            dialogStage.close();
        });
    }



    public static void main(String[] args) {
        launch(args);
    }

}
