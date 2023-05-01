#include "ServerListItemWidget.h"

#include <QtWidgets>

ServerListItemWidget::ServerListItemWidget(bc_server s, QWidget *parent)
	: QWidget{parent} {
	QString max = QString::number(s.max_players); 
	QString online = QString::number(s.online_players);
	QString finalc = (online + " / " + max);

    _layout = new QGridLayout(this);
    _image = new QLabel(this);
    _name = new QLabel(s.name, this);
    _version = new QLabel(s.version, this);
    _players = new QLabel(finalc, this);
    _description = new QLabel(s.description, this);

	if (s.icon != NULL) {
		QByteArray imageencoded = s.icon;
		QByteArray imagebytes = QByteArray::fromBase64(imageencoded);

		QPixmap pic;
		pic.loadFromData(imagebytes, "PNG");

		_image->setPixmap(pic.scaled(64, 64, Qt::KeepAspectRatio, Qt::SmoothTransformation));
	}

	_image->setStyleSheet(".QLabel { margin-right: 4px; }");
	_name->setStyleSheet(".QLabel { font-size: 15px; font-weight: bold; }");

	_layout->addWidget(_image, 0, 0, 3, 1, Qt::AlignLeft);
	_layout->addWidget(_name, 0, 1, 1, 1, Qt::AlignLeft);
	_layout->addWidget(_version, 0, 3, 1, 1, Qt::AlignRight);
	_layout->addWidget(_players, 1, 3, 1, 1, Qt::AlignRight);
	_layout->addWidget(_description, 1, 1, 1, 1, Qt::AlignLeft);

	_layout->setColumnStretch(2, 1);

	_layout->setAlignment(Qt::AlignTop);

	_layout->setSpacing(0);
	_layout->setContentsMargins(3, 3, 3, 3);

	setStyleSheet("QLabel { font-size: 11px; }");

	setLayout(_layout);
}
