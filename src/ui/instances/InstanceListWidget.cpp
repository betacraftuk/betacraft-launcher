#include "InstanceListWidget.h"

#include "../Betacraft.h"
#include <QtWidgets>

InstanceListWidget::InstanceListWidget(QWidget *parent)
	: QWidget{parent} {
	_layout = new QGridLayout(this);
	_instanceList = new QTreeWidget(this);
	_addInstanceButton = new QPushButton(this);
	_addInstanceWidget = new AddInstanceWidget();
	_instanceEditWidget = new InstanceEditWidget();

	_addInstanceButton->setText(bc_translate("instance_add_button"));

	_instanceList->header()->setHidden(1);
	_instanceList->setIconSize(QSize(28, 28));

	_instanceList->setDragDropMode(QAbstractItemView::InternalMove);

	_instanceList->setContextMenuPolicy(Qt::CustomContextMenu);
	_instanceList->viewport()->installEventFilter(this);

	populateInstanceList();

	_layout->addWidget(_addInstanceButton, 0, 0, 1, 11);
	_layout->addWidget(_instanceList, 1, 0, 1, 11);

	_layout->setSpacing(0);
	_layout->setContentsMargins(5, 5, 5, 5);

	setLayout(_layout);

	connect(_addInstanceButton, SIGNAL(released()), this, SLOT(onAddInstanceClicked()));
	connect(_addInstanceWidget, SIGNAL(signal_instanceAdded()), this, SLOT(onInstanceAdded()));
	connect(_instanceEditWidget, SIGNAL(signal_instanceSettingsSaved()), this, SLOT(onInstanceSettingsSaved()));
	connect(_instanceList, SIGNAL(itemClicked(QTreeWidgetItem*, int)), this, SLOT(onInstanceClicked(QTreeWidgetItem*, int)));
}

void InstanceListWidget::onInstanceClicked(QTreeWidgetItem* item, int column) {
	bc_instance instance = item->data(0, Qt::UserRole).value<bc_instance>();

	if (instance.path[0] == '\0') {
		return;
	}

	QString id(instance.path);

	if (!id.isNull() && id.compare(_selectedInstance) != 0) {
		bc_instance_select(id.toStdString().c_str());
		populateInstanceList();

		emit signal_instanceUpdate();
	}
}

QTreeWidgetItem* InstanceListWidget::instanceListAddItem(bc_instance instance) {
    QTreeWidgetItem* newItem = new QTreeWidgetItem();
    QString icon_path = QString("%1%2").arg(QString(instance.path).split("bc_instance.json")[0]).arg("instance_icon.png");
    QString icon = ":/assets/unknown_pack.png";

    if (QFile::exists(icon_path)) {
        icon = icon_path;
    }

    QVariant v;
	bc_instance instanceCopy = instance;
    v.setValue(instanceCopy);

    newItem->setData(0, Qt::UserRole, v);
    newItem->setIcon(0, QIcon(icon));
	newItem->setText(0, instance.name);
    newItem->setSizeHint(0, QSize(32, 32));

    if (_selectedInstance.compare(instance.path) == 0) {
        newItem->setForeground(0, QBrush(QColor(0,0,0,255)));
        newItem->setBackground(0, QBrush(QColor(86, 184, 91, 255)));
    }

	return newItem;
}

void InstanceListWidget::populateInstanceList() {
	QSet<int> groups;

	for (int i = 0; i < _instanceList->topLevelItemCount(); i++) {
		if (_instanceList->topLevelItem(i)->isExpanded()) {
			groups.insert(i);
		}
	}

	_instanceList->clear();

	bc_instance_array* instancesStandalone = bc_instance_get_all();
	bc_instance_group_array* instancesGrouped = bc_instance_group_get_all();
	bc_instance* instance_selected = bc_instance_select_get();

	if (instance_selected != NULL) {
		_selectedInstance = QString(instance_selected->path);
	}

	for (int i = 0; i < instancesStandalone->len; i++) {
		QTreeWidgetItem* item = instanceListAddItem(instancesStandalone->arr[i]);
		item->setFlags(item->flags() ^ Qt::ItemIsDropEnabled);

		_instanceList->insertTopLevelItem(i, item);
	}

	for (int i = 0; i < instancesGrouped->len; i++) {
		QTreeWidgetItem* groupItem = new QTreeWidgetItem();
		groupItem->setText(0, instancesGrouped->arr[i].group_name);
		groupItem->setSizeHint(0, QSize(32, 32));
		groupItem->setWhatsThis(0, "group");
		groupItem->setExpanded(1);
		groupItem->setFlags(groupItem->flags() ^ Qt::ItemIsDragEnabled);

		_instanceList->insertTopLevelItem(0, groupItem);

		for (int y = 0; y < instancesGrouped->arr[i].len; y++) {
			QTreeWidgetItem* item = instanceListAddItem(instancesGrouped->arr[i].instances[y]);
			item->setFlags(item->flags() ^ Qt::ItemIsDropEnabled);

			groupItem->addChild(item);
            _instanceList->insertTopLevelItem(i, item);
		}
	}

	free(instancesStandalone);
	free(instancesGrouped);
	free(instance_selected);

	for (auto g : groups) {
		_instanceList->topLevelItem(g)->setExpanded(1);
	}
}

void InstanceListWidget::onInstanceSettingsSaved() {
	_instanceEditWidget->close();
	populateInstanceList();

	emit signal_instanceUpdate();
}

void InstanceListWidget::onInstanceAdded() {
	_addInstanceWidget->close();
	populateInstanceList();
}

void InstanceListWidget::onAddInstanceClicked() {
	_addInstanceWidget->populateGroupList();
    _addInstanceWidget->_instanceNameTextbox->setText("");
    _addInstanceWidget->_versionWidget->clean();
	_addInstanceWidget->show();
}

void InstanceListWidget::menuTrigger(QAction* action) {
	QString atype = action->whatsThis();
	QModelIndex modelIndex = action->data().toModelIndex();

	if (_instanceList->itemFromIndex(modelIndex)->whatsThis(0).compare("group") == 0) {
		QString groupName = _instanceList->itemFromIndex(modelIndex)->text(0);
		bc_instance_remove_group(groupName.toStdString().c_str());
		populateInstanceList();

		return;
	}

	bc_instance instance = _instanceList->itemFromIndex(modelIndex)->data(0, Qt::UserRole).value<bc_instance>();

	if (atype == "edit") {
		_instanceEditWidget->setInstance(instance);
		_instanceEditWidget->show();
	} else if (atype == "remove") {
		bc_instance_remove(instance.path);
		populateInstanceList();
		emit signal_instanceUpdate();
	}
}

bool InstanceListWidget::eventFilter(QObject* source, QEvent* event) {
	if (event->type() == QEvent::MouseButtonRelease  && source == _instanceList->viewport()) {
		QMouseEvent* mouseEvent = static_cast<QMouseEvent*>(event);

		if (mouseEvent->button() == Qt::RightButton) {
			QTreeWidgetItem* item = _instanceList->itemAt(mouseEvent->position().toPoint());

			if (item != NULL) {
				if (item->whatsThis(0).compare("group") == 0) {
					QModelIndex index = _instanceList->indexFromItem(item);

					_actRemove = new QAction("Remove", this);
					_actRemove->setData(index);
					_actRemove->setWhatsThis("remove");

					QMenu* newMenu = new QMenu();
					newMenu->addActions(
                    {
                        _actRemove,
                    });

					connect(newMenu, SIGNAL(triggered(QAction*)), this, SLOT(menuTrigger(QAction*)));
					newMenu->exec(mouseEvent->globalPosition().toPoint());

					return true;

				} else {
					QModelIndex index = _instanceList->indexFromItem(item);

					_actEdit = new QAction("Edit", this);
					_actEdit->setData(index);
					_actEdit->setWhatsThis("edit");

					_actRemove = new QAction("Remove", this);
					_actRemove->setData(index);
					_actRemove->setWhatsThis("remove");

					QMenu *newMenu = new QMenu();
					newMenu->addActions(
					{
						_actEdit,
						_actRemove,
					});

					connect(newMenu, SIGNAL(triggered(QAction*)), this, SLOT(menuTrigger(QAction*)));
					newMenu->exec(mouseEvent->globalPosition().toPoint());

					return true;
				}
			}
		}
	}

	if (event->type() == QEvent::Drop) {
		QDropEvent* dropEvent = static_cast<QDropEvent*>(event);
		QTreeWidgetItem* item = _instanceList->itemAt(dropEvent->position().toPoint());

		if (item == NULL || item->whatsThis(0).compare("group") == 0) {
            return true;
		}

		QTimer::singleShot(0, this, SLOT(moveInstanceList()));
	}

	return false;
}

void InstanceListWidget::moveInstanceList() {
    bc_instance_array* instancesStandalone = new bc_instance_array;
    bc_instance_group_array* instancesGrouped = new bc_instance_group_array;

	instancesStandalone->len = 0;
	instancesGrouped->len = 0;

    for (int i = 0; i < _instanceList->model()->rowCount(); i++) {
        QModelIndex index = _instanceList->model()->index(i, 0);
		QTreeWidgetItem* item = _instanceList->itemFromIndex(index);

		if (item->whatsThis(0).compare("group") == 0) {
			bc_instance_group* group = &instancesGrouped->arr[instancesGrouped->len];
			snprintf(group->group_name, sizeof(group->group_name), "%s", item->text(0).toStdString().c_str());

			for (int j = 0; j < item->childCount(); j++) {
				int len = group->len;
				bc_instance instance = item->child(j)->data(0, Qt::UserRole).value<bc_instance>();

				group->instances[len] = instance;
				group->len++;
			}

			instancesGrouped->len++;
		} else {
			bc_instance instance = item->data(0, Qt::UserRole).value<bc_instance>();

			instancesStandalone->arr[instancesStandalone->len] = instance;
			instancesStandalone->len++;
		}
    }

    bc_instance_move(instancesStandalone, instancesGrouped, _selectedInstance.toStdString().c_str());

	delete instancesStandalone;
	delete instancesGrouped;
}
