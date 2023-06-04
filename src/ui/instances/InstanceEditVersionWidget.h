#ifndef INSTANCEEDITVERSIONWIDGET_H
#define INSTANCEEDITVERSIONWIDGET_H

#include <QWidget>

extern "C" {
    #include "../../core/Instance.h"
}

class QGridLayout;
class QPushButton;
class QLineEdit;
class QTabBar;
class QTreeView;
class QStandardItemModel;
class QModelIndex;

class InstanceEditVersionWidget : public QWidget
{
    Q_OBJECT
public:
    explicit InstanceEditVersionWidget(QWidget* parent = nullptr);
    QString getSettings();
    void setInstance(bc_instance instance);
    void clean();

private slots:
    void onSearchButtonClicked();
    void onMenuTabChanged(int index);

protected:
    void keyPressEvent(QKeyEvent* e);

private:
    QGridLayout* _layout;
    QPushButton* _searchButton;
    QLineEdit* _searchTextbox;
    QTabBar* _menu;
    QTreeView* _versionsTreeView;
    QStandardItemModel* _versionListAll;
    QStandardItemModel* _versionListRelease;
    QStandardItemModel* _versionListOldBeta;
    QStandardItemModel* _versionListOldAlpha;
    QStandardItemModel* _versionListSnapshot;
    QStandardItemModel* _versionListSearch;
    void versionListInit();
    void setSelectedInstance();
    QString _version;
};

#endif
