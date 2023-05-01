#include "AddAccountWidget.h"
#include "AddAccountMicrosoftWidget.h"
#include "AddAccountMojangWidget.h"

#include <QtWidgets>

extern "C" {
	#include "../../core/Betacraft.h"
}

AddAccountWidget::AddAccountWidget(QWidget* parent)
	: QWidget{ parent } {
	char tr[256];

	_layout = new QGridLayout(this);
	_menu = new QTabWidget(this);
	_microsoftWidget = new AddAccountMicrosoftWidget(this);
	//_mojangWidget = new AddAccountMojangWidget(this);

	_menu->setStyleSheet("QTabWidget::pane { border: 0; }");
	bc_translate("accounts_microsoft_title", tr);
	_menu->addTab(_microsoftWidget, QString(tr));

	//_menu->addTab(_mojangWidget, bc_translate("accounts_mojang_title"));
	bc_translate("accounts_mojang_title", tr);
	_menu->addTab(new QWidget(), QString(tr));
	_menu->setTabEnabled(1, 0);

	_layout->setAlignment(Qt::AlignTop);

	_layout->setSpacing(0);
	_layout->setContentsMargins(0, 0, 0, 0);

	_layout->addWidget(_menu, 0, 0, 1, 11);

	setLayout(_layout);

	bc_translate("accounts_creation_title", tr);
	setWindowTitle(QString(tr));
	resize(400, 200);
	setMinimumSize(400, 200);

	connect(_microsoftWidget, SIGNAL(signal_accountAddSuccess()), this, SIGNAL(signal_accountAddSuccess()));

	setWindowModality(Qt::ApplicationModal);
}
