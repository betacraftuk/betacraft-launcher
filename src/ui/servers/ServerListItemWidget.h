#ifndef SERVERLISTITEMWIDGET_H
#define SERVERLISTITEMWIDGET_H

#include <QWidget>

extern "C" {
	#include "../../core/Betacraft.h"
}

class QGridLayout;
class QLabel;
class QPushButton;

class ServerListItemWidget : public QWidget
{
	Q_OBJECT
public:
	explicit ServerListItemWidget(bc_server s, std::unordered_map<QString, QByteArray> serverToIconMap, QWidget *parent = nullptr);

private:
	QGridLayout* _layout;
	QLabel* _image;
	QLabel* _name;
	QLabel* _version;
	QLabel* _players;
	QLabel* _description;
	QPushButton* _readMore;
};

#endif
