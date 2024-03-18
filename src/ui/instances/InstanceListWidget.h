#ifndef INSTANCELISTWIDGET_H
#define INSTANCELISTWIDGET_H

#include <QWidget>

#include "AddInstanceWidget.h"
#include "InstanceEditWidget.h"

class QGridLayout;
class QTreeWidget;
class QPushButton;
class QTreeWidgetItem;

class InstanceListWidget : public QWidget {
    Q_OBJECT
  public:
    explicit InstanceListWidget(QWidget *parent = nullptr);

  signals:
    void signal_instanceUpdate();

  private slots:
    void menuTrigger(QAction *action);
    void onAddInstanceClicked();
    void onInstanceAdded();
    void onInstanceSettingsSaved();
    void onInstanceClicked(QTreeWidgetItem *item, int column);
    void moveInstanceList();

  private:
    bool eventFilter(QObject *source, QEvent *event);
    QGridLayout *_layout;
    AddInstanceWidget *_addInstanceWidget;
    InstanceEditWidget *_instanceEditWidget;
    QTreeWidget *_instanceList;
    QPushButton *_addInstanceButton;
    QAction *_actEdit;
    QAction *_actRemove;
    QString _selectedInstance;
    void populateInstanceList();
    QTreeWidgetItem *instanceListAddItem(bc_instance instance);
};

#endif
