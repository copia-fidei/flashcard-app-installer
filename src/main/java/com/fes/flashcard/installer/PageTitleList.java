package com.fes.flashcard.installer;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import static javax.swing.BorderFactory.createMatteBorder;

public class PageTitleList extends JList<String> {

	public PageTitleList() {
		super();

		setEnabled(false);
		setCellRenderer(new UnderlineSelected());
	}

	static class UnderlineSelected extends JLabel implements ListCellRenderer<Object> {

		private final Font normalFont;
		private final Font underlinedFont;

		{
			normalFont = UIManager.getFont("List.font");
			Map<TextAttribute, Object> attrs = new HashMap<>(normalFont.getAttributes());
			attrs.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			underlinedFont = normalFont.deriveFont(attrs);
		}

		public UnderlineSelected() {
			setOpaque(true);
		}

		public Component getListCellRendererComponent(JList<?> list,
		                                              Object value,
		                                              int index,
		                                              boolean isSelected,
		                                              boolean cellHasFocus) {

			setText(value.toString());

//			if (isSelected) {
//				setFont(underlinedFont);
//			}
//			else {
//				setFont(normalFont);
//			}
			setBorder(isSelected ? createMatteBorder(0, 0, 2, 0, list.getSelectionBackground()) : null);
//			setBorder(isSelected
//					? BorderFactory.createDashedBorder(null)
//					: null);

			setBackground(list.getBackground());
			setForeground(list.getForeground());


			return this;
		}
	}
}
