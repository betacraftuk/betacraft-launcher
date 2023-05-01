#include "AddAccountMicrosoftWidget.h"

#include <QtWidgets>

extern "C" {
	#include "../../core/AuthMicrosoft.h"
	#include "../../core/Betacraft.h"
}

bc_auth_microsoftDeviceResponse _authDeviceRes;

AddAccountMicrosoftWidget::AddAccountMicrosoftWidget(QWidget* parent)
	: QWidget{ parent } {
	char tr[256];

	_layout = new QGridLayout(this);
	_microsoftLink = new QLabel(this);
	_code = new QLabel(this);
	_proceedText = new QLabel(this);
	_typeCodeText = new QLabel(this);

	bc_translate("accounts_microsoft_instruction1", tr);
	_proceedText->setText(QString(tr));
	bc_translate("accounts_microsoft_instruction2", tr);
	_typeCodeText->setText(QString(tr));

	QFont font;
	font.setPointSize(12);

	_proceedText->setFont(font);
	_typeCodeText->setFont(font);
	_microsoftLink->setFont(font);

	_microsoftLink->setText("<a href=\"https://www.microsoft.com/link\">https://www.microsoft.com/link</a>");
	_microsoftLink->setTextFormat(Qt::RichText);
	_microsoftLink->setTextInteractionFlags(Qt::TextBrowserInteraction | Qt::TextSelectableByMouse);
	_microsoftLink->setOpenExternalLinks(true);

	font.setBold(true);
	font.setPointSize(14);

	_code->setTextInteractionFlags(Qt::TextSelectableByMouse);
	_code->setFont(font);

	_layout->setAlignment(Qt::AlignVCenter);

	_layout->addWidget(_proceedText, 0, 0, Qt::AlignCenter);
	_layout->addWidget(_microsoftLink, 1, 0, Qt::AlignCenter);
	_layout->addWidget(_typeCodeText, 2, 0, Qt::AlignCenter);
	_layout->addWidget(_code, 3, 0, Qt::AlignCenter);

	_layout->setSpacing(5);
	_layout->setContentsMargins(0, 0, 0, 0);

	setLayout(_layout);

	connect(&_watcher, SIGNAL(finished()), this, SIGNAL(signal_accountAddSuccess()));
}

void AddAccountMicrosoftWidget::showEvent(QShowEvent* event) {
	QWidget::showEvent(event);

	if (_code->text().isEmpty()) {
		QTimer::singleShot(0, this, SLOT(Authenticate()));
	}
}

void AddAccountMicrosoftWidget::Authenticate() {
	bc_auth_microsoftDeviceResponse* res = bc_auth_microsoft_device();
	_authDeviceRes = *res;
	free(res);

	_code->setText(_authDeviceRes.user_code);

	QFuture<void> future = QtConcurrent::run(bc_auth_microsoft, &_authDeviceRes);
	_watcher.setFuture(future);
}
