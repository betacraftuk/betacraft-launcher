#include "InstanceEditModVersionsWidget.h"
#include "../../Betacraft.h"

#include <QtWidgets>
#include <limits.h>

char _path[PATH_MAX];
char _version[32];
bc_mod_version _selectedModVersion;

InstanceEditModVersionsWidget::InstanceEditModVersionsWidget(QWidget* parent)
	: QWidget{ parent } {
	_layout = new QGridLayout(this);
	_versionList = new QListWidget(this);

	_layout->addWidget(_versionList);

	_layout->setSpacing(5);
	_layout->setContentsMargins(10, 10, 10, 10);

	setLayout(_layout);

    setWindowTitle(bc_translate("mods_mod_version_window_title"));
	resize(250, 400);
	setMinimumSize(250, 400);
	
	connect(_versionList, SIGNAL(itemClicked(QListWidgetItem*)), this, SLOT(onVersionClicked(QListWidgetItem*)));
	connect(&_watcher, SIGNAL(finished()), this, SIGNAL(signal_ModDownloadFinished()));

	setWindowModality(Qt::ApplicationModal);
}

void InstanceEditModVersionsWidget::populateList(bc_mod_version_array versions) {
	_versionList->clear(); 

	for (int i = 0; i < versions.len; i++) {
        QListWidgetItem* item = new QListWidgetItem();

        QVariant version;
        version.setValue(versions.arr[i]);

        item->setData(Qt::UserRole, version);
        item->setText(QString(versions.arr[i].version));
        _versionList->addItem(item);
	}
}

void InstanceEditModVersionsWidget::setInstance(bc_instance instance) {
	snprintf(_path, sizeof(_path), "%s", instance.path);
	snprintf(_version, sizeof(_version), "%s", instance.version);
}

void InstanceEditModVersionsWidget::onVersionClicked(QListWidgetItem* item) {
	_selectedModVersion = item->data(Qt::UserRole).value<bc_mod_version>();

    QFuture<void> future = QtConcurrent::run(bc_mod_download, &_selectedModVersion, _path, _version);
	_watcher.setFuture(future);

	emit signal_ModDownloadStarted();
}
