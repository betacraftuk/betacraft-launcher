#ifndef SERVERLISTWIDGET_H
#define SERVERLISTWIDGET_H

#include <QWidget>

extern "C" {
	#include "../../core/Betacraft.h"
}

class QGridLayout;
class QLineEdit;
class QListWidget;
class QListWidgetItem;
class QPushButton;

class ServerListWidget : public QWidget
{
	Q_OBJECT
public:
	explicit ServerListWidget(QWidget *parent = nullptr);

private slots:
	void onSearchButton();
	void onServerClicked(QListWidgetItem* item);

protected:
	void keyPressEvent(QKeyEvent* e);

private:
	QGridLayout* _layout;
	QLineEdit* _searchTextBox;
	QListWidget* _serverList;
	QPushButton* _searchButton;
	void serverListAdd(int index);
	//bc_server_array* _serverArray;
};

#endif
