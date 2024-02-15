#ifndef INSTANCEEDITMODVERSIONSWIDGET_H
#define INSTANCEEDITMODVERSIONSWIDGET_H

#include <QtGui>

extern "C" {
    #include "../../../core/Mod.h"
    #include "../../../core/Instance.h"
}

class QGridLayout;
class QListWidget;
class QListWidgetItem;

class InstanceEditModVersionsWidget : public QWidget {
    Q_OBJECT
public:
    explicit InstanceEditModVersionsWidget(QWidget* parent = NULL);
    void populateList(bc_mod_version_array versions);
    void setInstance(bc_instance instance);

private slots:
    void onVersionClicked(QListWidgetItem* item);

signals:
    void signal_ModDownloadStarted();
    void signal_ModDownloadFinished();

protected:

private:
    QGridLayout* _layout;
    QListWidget* _versionList;
    QFutureWatcher<void> _watcher;
};

#endif
