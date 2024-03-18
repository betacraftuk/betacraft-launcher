#ifndef ADDINSTANCEWIDGET_H
#define ADDINSTANCEWIDGET_H

#include "InstanceEditVersionWidget.h"
#include <QWidget>

class QGridLayout;
class QLineEdit;
class QComboBox;
class QListWidget;
class QPushButton;

class AddInstanceWidget : public QWidget {
    Q_OBJECT
  public:
    explicit AddInstanceWidget(QWidget *parent = nullptr);
    void populateGroupList();
    QLineEdit *_instanceNameTextbox;
    InstanceEditVersionWidget *_versionWidget;

  private slots:
    void onCreateButtonClicked();
    void onNewGroupButtonClicked();

  protected:
    void keyPressEvent(QKeyEvent *e);

  signals:
    void signal_instanceAdded();

  private:
    QGridLayout *_layout;
    QLineEdit *_newGroupTextbox;
    QListWidget *_groupList;
    QPushButton *_createButton;
    QPushButton *_newGroupButton;
    void populateVersionList();
};

#endif
