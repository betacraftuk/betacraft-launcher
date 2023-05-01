#include "AddInstanceWidget.h"

#include <QtWidgets>

extern "C" {
	#include "../../core/Instance.h"
	#include "../../core/Betacraft.h"
	#include "../../core/VersionList.h"
}

AddInstanceWidget::AddInstanceWidget(QWidget* parent)
	: QWidget{ parent } {
	char tr[256];

	_layout = new QGridLayout(this);
	_instanceNameTextbox = new QLineEdit(this);
	_gameVersionDropdown = new QComboBox(this);
	_groupList = new QListWidget(this);
	_createButton = new QPushButton(this);
	_newGroupButton = new QPushButton(this);
	_newGroupTextbox = new QLineEdit(this);

	bc_translate("instance_create_button", tr);
	_createButton->setText(QString(tr));
	bc_translate("instance_group_new", tr);
	_newGroupButton->setText(QString(tr));

	_layout->setAlignment(Qt::AlignTop);

	populateVersionList();

	bc_translate("instance_name", tr);
	_layout->addWidget(new QLabel(QString(tr)), 0, 0, 1, 11);
	_layout->addWidget(_instanceNameTextbox, 1, 0, 1, 11);
	bc_translate("instance_game_version", tr);
	_layout->addWidget(new QLabel(QString(tr)), 2, 0, 1, 11);
	_layout->addWidget(_gameVersionDropdown, 3, 0, 1, 11);
	bc_translate("instance_group", tr);
	_layout->addWidget(new QLabel(QString(tr)), 4, 0, 1, 11);
	_layout->addWidget(_newGroupTextbox, 5, 0, 1, 10);
	_layout->addWidget(_newGroupButton, 5, 10, 1, 1);
	_layout->addWidget(_groupList, 6, 0, 1, 11);
	_layout->addWidget(_createButton, 7, 0, 1, 11);

	_layout->setRowMinimumHeight(5, 0);

	_layout->setSpacing(5);
	_layout->setContentsMargins(10, 10, 10, 10);

	setLayout(_layout);
	setStyleSheet("QLabel { font-size: 14px; }");

	bc_translate("instance_creation_title", tr);
	setWindowTitle(QString(tr));
	resize(300, 400);
	setMinimumSize(300, 400);

	connect(_createButton, SIGNAL(released()), this, SLOT(onCreateButtonClicked()));
	connect(_newGroupButton, SIGNAL(released()), this, SLOT(onNewGroupButtonClicked()));

	setWindowModality(Qt::ApplicationModal);
}

void AddInstanceWidget::onCreateButtonClicked() {
	if (!_instanceNameTextbox->text().trimmed().isEmpty()) {
		QString url = _gameVersionDropdown->currentData().toString();
		QString version = _gameVersionDropdown->currentText();
		QString name = _instanceNameTextbox->text().trimmed();
		QString group;

		if (_groupList->currentRow() != -1) {
			group = _groupList->currentItem()->text();
		}

		bc_instance_create(
			name.toStdString().c_str(),
			version.toStdString().c_str(),
			url.toStdString().c_str(),
			group.isNull() ? NULL : group.toStdString().c_str());

		emit signal_instanceAdded();
	}
}

void AddInstanceWidget::onNewGroupButtonClicked() {
	if (!_newGroupTextbox->text().isEmpty()) {
		bc_instance_group_create(_newGroupTextbox->text().trimmed().toStdString().c_str());
		populateGroupList();
	}
}

void AddInstanceWidget::populateVersionList() {
	bc_versionlist* versions = bc_versionlist_fetch();

	for (int i = 0; i < versions->versions_len; i++) {
		QVariant q;
		q.setValue(QString(versions->versions[i].url));

		_gameVersionDropdown->addItem(QString(versions->versions[i].id), q);
	}

	free(versions);
}

void AddInstanceWidget::populateGroupList() {
	_groupList->clear();
	bc_instance_group_name_array* groups = bc_instance_group_name_get_all();

	_newGroupTextbox->setText("");

	for (int i = 0; i < groups->len; i++) {
		_groupList->addItem(groups->arr[i]);
	}

	free(groups);
}

void AddInstanceWidget::keyPressEvent(QKeyEvent *e) {
	if(e->key() == Qt::Key_Return) {
		onCreateButtonClicked();
	}
}
