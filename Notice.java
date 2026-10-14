// 안내/경고/오류 팝업 (모달이 아닌 방식으로 띄워 macOS에서 팝업 종료 후 창이 먹통 되는 문제 방지)

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public final class Notice {
    private Notice() {}

    public static void show(Component parent, Object message, String title, int messageType) {
        show(parent, message, title, messageType, null);
    }

    public static void show(Component parent, Object message, String title, int messageType, Runnable onClose) {
        JOptionPane pane = new JOptionPane(message, messageType);
        JDialog dialog = pane.createDialog(parent, title);
        dialog.setModal(false);
        dialog.setAlwaysOnTop(true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // [수정] alwaysOnTop 팝업이 사라진 뒤 macOS에서 메인 창이 포커스를 다시 못 받아
        // 탭/버튼 클릭이 먹통되는 문제 방지 - 팝업이 닫히면 원래 창을 강제로 앞으로 가져오고 포커스를 줌
        dialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                if (onClose != null) {
                    onClose.run();
                }
                Window owner = SwingUtilities.getWindowAncestor(parent);
                if (owner != null) {
                    owner.toFront();
                    owner.requestFocus();
                }
            }
        });

        dialog.setVisible(true);
    }
}