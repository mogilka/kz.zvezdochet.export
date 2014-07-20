package kz.zvezdochet.export.handler;

import kz.zvezdochet.bean.Event;
import kz.zvezdochet.core.handler.Handler;
import kz.zvezdochet.core.ui.util.DialogUtil;
import kz.zvezdochet.core.util.DateUtil;
import kz.zvezdochet.export.exporter.HTMLExporter;
import kz.zvezdochet.parts.EventPart;
import kz.zvezdochet.util.Configuration;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

/**
 * Обработка события для html-экспорта индивидуального гороскопа
 * @author Nataly Didenko
 *
 */
public class ExportHandler extends Handler {
	@Execute
	public void execute(@Active MPart activePart) {
		try {
			EventPart eventPart = (EventPart)activePart.getObject();
			final Event event = (Event)eventPart.getModel(EventPart.MODE_CALC, true);
			if (null == event) return;
			if (null == event.getConfiguration()) {
				DialogUtil.alertWarning("Произведите расчёт");
				return;
			}
			updateStatus("Экспорт индивидуального гороскопа", false);
    		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
    			@Override
    			public void run() {
	        		new HTMLExporter().generate(event);
    			}
    		});
			updateStatus("Экспорт завершён", false);
		} catch (Exception e) {
			DialogUtil.alertError(e.getMessage());
			updateStatus("Ошибка экспорта", true);
			e.printStackTrace();
		}
	}
}
