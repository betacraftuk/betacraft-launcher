#include "ServerListWidget.h"
#include "ServerListItemWidget.h"

#include <QtWidgets>

extern "C" {
	#include "../../core/Instance.h"
}

bc_server_array _serverArray;

ServerListWidget::ServerListWidget(QWidget *parent)
	: QWidget{parent} {
	char tr[256];

	_layout = new QGridLayout(this);
	_searchTextBox = new QLineEdit(this);
	_searchButton = new QPushButton(this);
	_serverList = new QListWidget(this);

	bc_translate("serverlist_search_button", tr);
	_searchButton->setText(QString(tr));

	bc_translate("serverlist_search_placeholder", tr);
	_searchTextBox->setPlaceholderText(QString(tr));

	_serverList->setVerticalScrollBarPolicy(Qt::ScrollBarAlwaysOn);
	_serverList->setVerticalScrollMode(QAbstractItemView::ScrollPerPixel);

	populateServerList();

	_layout->addWidget(_searchTextBox, 1, 0, 1, 10);
	_layout->addWidget(_searchButton, 1, 10, 1, 1);
	_layout->addWidget(_serverList, 2, 0, 1, 11);

	_layout->setAlignment(Qt::AlignTop);

	_layout->setSpacing(0);
	_layout->setContentsMargins(5, 5, 5, 5);

	setLayout(_layout);

	connect(_searchButton, SIGNAL(released()), this, SLOT(onSearchButton()));
	connect(_serverList, SIGNAL(itemClicked(QListWidgetItem*)), this, SLOT(onServerClicked(QListWidgetItem*)));
}

void ServerListWidget::onServerClicked(QListWidgetItem* item) {
	QString ip = item->data(Qt::UserRole).toString();

	bc_instance_run(ip.toStdString().c_str(), NULL);
}

void ServerListWidget::addServerItem(bc_server server) {
    QListWidgetItem* item = new QListWidgetItem();
    ServerListItemWidget* serverItem = new ServerListItemWidget(server);
    QVariant q;

    q.setValue(QString(server.connect_protocol));

    item->setData(Qt::UserRole, q);
    item->setFlags(item->flags() & ~Qt::ItemIsSelectable);
    item->setSizeHint(serverItem->sizeHint());

    _serverList->addItem(item);
    _serverList->setItemWidget(item, serverItem);
}

void ServerListWidget::populateServerList() {
	bc_server_array* servers = bc_server_list();
	_serverArray.len = servers->len;

	for (int i = 0; i < servers->len; i++) {
		_serverArray.arr[i] = servers->arr[i];
		addServerItem(servers->arr[i]);
	}

	for (int i = 0; i < servers->len; i++) {
		free(servers->arr[i].icon);
	}

	free(servers);
}

void ServerListWidget::onSearchButton() {
	_serverList->clear();

	QString search = _searchTextBox->text().trimmed().toLower();

	for (int i = 0; i < _serverArray.len; i++) {
		if (QString(_serverArray.arr[i].name).contains(search, Qt::CaseInsensitive)
			|| QString(_serverArray.arr[i].description).contains(search, Qt::CaseInsensitive)
			|| QString(_serverArray.arr[i].connect_version).contains(search, Qt::CaseInsensitive)) {
			addServerItem(_serverArray.arr[i]);
		}
	}
}

void ServerListWidget::keyPressEvent(QKeyEvent* e) {
	if (e->key() == Qt::Key_Return) {
		onSearchButton();
	}
}
