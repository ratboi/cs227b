package apps.server.error;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import server.event.ServerConnectionErrorEvent;
import server.event.ServerIllegalMoveEvent;
import server.event.ServerTimeoutEvent;
import util.observer.Event;
import util.observer.Observer;
import apps.common.table.JZebraTable;

@SuppressWarnings("serial")
public final class ErrorPanel extends JPanel implements Observer
{

	private final JZebraTable errorTable;

	public ErrorPanel()
	{
		super(new GridBagLayout());

		DefaultTableModel model = new DefaultTableModel();
		model.addColumn("Role");
		model.addColumn("Error");

		errorTable = new JZebraTable(model)
		{

			@Override
			public boolean isCellEditable(int rowIndex, int colIndex)
			{
				return false;
			}
		};
		errorTable.setShowHorizontalLines(true);
		errorTable.setShowVerticalLines(true);

		this.add(new JScrollPane(errorTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
	}

	public void observe(Event event)
	{
		if (event instanceof ServerConnectionErrorEvent)
		{
			observe((ServerConnectionErrorEvent) event);
		}
		else if (event instanceof ServerIllegalMoveEvent)
		{
			observe((ServerIllegalMoveEvent) event);
		}
		else if (event instanceof ServerTimeoutEvent)
		{
			observe((ServerTimeoutEvent) event);
		}
	}

	private synchronized void observe(ServerConnectionErrorEvent event)
	{
		DefaultTableModel model = (DefaultTableModel) errorTable.getModel();
		String role = event.getRole().getName().toString();
		String error = "Connection Error";

		model.addRow(new String[] { role, error });
	}

	private synchronized void observe(ServerIllegalMoveEvent event)
	{
		DefaultTableModel model = (DefaultTableModel) errorTable.getModel();
		String role = event.getRole().toString();
		String error = "Illegal Move: " + event.getMove().toString();

		model.addRow(new String[] { role, error });
	}

	private synchronized void observe(ServerTimeoutEvent event)
	{
		DefaultTableModel model = (DefaultTableModel) errorTable.getModel();
		String role = event.getRole().toString();
		String error = "Timeout";

		model.addRow(new String[] { role, error });
	}

}
