package com.attempt.npoc.utils;

import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class LoadingSpinner extends StackPane {
    private Arc arc1;
    private Arc arc2;
    private Text label;
    public LoadingSpinner() {
        // 外部弧形
        arc1 = new Arc();
        arc1.setCenterX(50);
        arc1.setCenterY(50);
        arc1.setRadiusX(40);
        arc1.setRadiusY(40);
        arc1.setStartAngle(0);
        arc1.setLength(270);
        arc1.setType(ArcType.OPEN);
        arc1.setStroke(Color.CORNFLOWERBLUE);
        arc1.setStrokeWidth(8);
        arc1.setFill(null);

        // 内部弧形
        arc2 = new Arc();
        arc2.setCenterX(50);
        arc2.setCenterY(50);
        arc2.setRadiusX(30);
        arc2.setRadiusY(30);
        arc2.setStartAngle(90);
        arc2.setLength(270);
        arc2.setType(ArcType.OPEN);
        arc2.setStroke(Color.LIGHTSEAGREEN);
        arc2.setStrokeWidth(8);
        arc2.setFill(null);
        // 文字
        label = new Text("Loading...");
        label.setFill(Color.GREY);
        label.setStyle("-fx-font-size: 14px;");
        label.setTranslateY(50);
        label.setTranslateX(20);
        // 外部弧形旋转动画
        RotateTransition rotateTransition1 = new RotateTransition(Duration.seconds(2), arc1);
        rotateTransition1.setByAngle(360);
        rotateTransition1.setCycleCount(Timeline.INDEFINITE);
        rotateTransition1.setAutoReverse(false);
        rotateTransition1.play();

        // 内部弧形反向旋转动画
        RotateTransition rotateTransition2 = new RotateTransition(Duration.seconds(1.5), arc2);
        rotateTransition2.setByAngle(-360);
        rotateTransition2.setCycleCount(Timeline.INDEFINITE);
        rotateTransition2.setAutoReverse(false);
        rotateTransition2.play();

        // 添加到组件中
        getChildren().addAll(arc1, arc2,label);
    }

    @Override
    protected void layoutChildren() {
        // 使弧形和中心圆位于中心位置
        double centerX = (getWidth() - 100) / 2; // 100是组件的宽度
        double centerY = (getHeight() - 100) / 2; // 100是组件的高度

        arc1.setLayoutX(centerX);
        arc1.setLayoutY(centerY);

        arc2.setLayoutX(centerX);
        arc2.setLayoutY(centerY);
        
    }
}
