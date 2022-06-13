package ru.obvilion.launcher.utils;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
import javafx.scene.shape.Arc;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.obvilion.launcher.Vars;

public class StyleUtil {
    public static void createFadeAnimation(Node node, int fadeDuration, float to) {
        if (!Vars.useAnimations) {
            node.setOpacity(to);
            return;
        }

        FadeTransition ft = new FadeTransition(Duration.millis(fadeDuration), node);

        ft.setFromValue(node.getOpacity());
        ft.setToValue(to);
        ft.play();
    }

    public static void createFadeAnimation(Stage stage, int fadeDuration, float to) {
        if (!Vars.useAnimations) {
            stage.setOpacity(to);
            return;
        }

        final Animation animation = new Transition() {
            {
                setCycleDuration(Duration.millis(fadeDuration));
            }

            final float ansOpasity = (float) stage.getOpacity();
            protected void interpolate(double f) {
                float op = (float) (to * f + ansOpasity * (1 - f));

                stage.setOpacity(op);
            }
        };

        animation.play();
    }

    public static void changePosition(Node element, double toX, double toY, int durationAnimation) {
        if (!Vars.useAnimations) {
            element.setLayoutX(toX);
            element.setLayoutY(toY);
            return;
        }

        final Animation animation = new Transition() {
            {
                setCycleDuration(Duration.millis(durationAnimation));
            }

            final float ansLayoutX = (float) element.getLayoutX();
            final float ansLayoutY = (float) element.getLayoutY();
            protected void interpolate(double f) {
                float x = (float) (toX * f + ansLayoutX * (1 - f));
                float y = (float) (toY * f + ansLayoutY * (1 - f));

                element.setLayoutX(x);
                element.setLayoutY(y);
            }
        };
        animation.play();
    }

    public static void changeYPosition(Node element, double toY, int durationAnimation) {
        if (!Vars.useAnimations) {
            element.setLayoutY(toY);
            return;
        }

        final Animation animation = new Transition() {
            {
                setCycleDuration(Duration.millis(durationAnimation));
            }

            final float ansLayoutY = (float) element.getLayoutY();
            protected void interpolate(double f) {
                float y = (float) (toY * f + ansLayoutY * (1 - f));

                element.setLayoutY(y);
            }
        };
        animation.play();
    }

    public static void changeText(Labeled element, int durationAnimation, float toOpacity, float minOpacity, String toText) {
        if (!Vars.useAnimations) {
            element.setText(toText);
            return;
        }

        final float dt = toOpacity - minOpacity;
        final Animation animation = new Transition() {
            {
                setCycleDuration(Duration.millis(durationAnimation));
            }

            boolean changed = false;
            protected void interpolate(double f) {
                if (f < 0.5f) {
                    element.setOpacity(minOpacity + dt - dt * f * 2);
                } else {
                    if (!changed) {
                        element.setText(toText);
                        changed = true;
                    }

                    element.setOpacity(minOpacity + dt * (f - 0.5f) * 2);
                }
            }
        };
        animation.play();
    }

    public static void changeText(TextInputControl element, int durationAnimation, float toOpacity, float minOpacity, String toText) {
        if (!Vars.useAnimations) {
            element.setText(toText);
            return;
        }

        final Animation animation = new Transition() {
            {
                setCycleDuration(Duration.millis(durationAnimation));
            }

            final float dt  = toOpacity - minOpacity;
            boolean changed = false;

            protected void interpolate(double f) {
                if (f < 0.5f) {
                    element.setOpacity(minOpacity + dt - dt * f * 2);
                } else {
                    if (!changed) {
                        element.setText(toText);
                        changed = true;
                    }

                    element.setOpacity(minOpacity + dt * (f - 0.5f) * 2);
                }
            }
        };
        animation.play();
    }

    public static void changeArc(Arc element, int durationAnimation, float to) {
        if (!Vars.useAnimations) {
            element.setLength(to);
            return;
        }

        final Animation animation = new Transition() {
            {
                setCycleDuration(Duration.millis(durationAnimation));
            }

            final double from = element.getLength();
            final double dt   = to - from;

            protected void interpolate(double f) {
                element.setLength(from + dt * f);
            }
        };
        animation.play();
    }

    public static long last_started_to = 0;
    public static void to(Node element1, Node element2, int durationAnimation, Runnable onEnd) {
        if (!Vars.useAnimations) {
            element1.setVisible(false);
            element1.setOpacity(0);

            element2.setOpacity(1);
            element2.setVisible(true);
            return;
        }

        if (last_started_to + durationAnimation >= System.currentTimeMillis()) {
            return;
        }

        element2.setVisible(true);
        element1.setVisible(true);

        final Animation animation = new Transition() {
            {
                setCycleDuration(Duration.millis(durationAnimation));
            }

            protected void interpolate(double f) {
                element1.setOpacity(1 - f);
                element2.setOpacity(f);

                if (f == 1.0) {
                    //element1.setVisible(false);
                    onEnd.run();
                }
            }
        };
        animation.play();

        last_started_to = System.currentTimeMillis();
    }
}
