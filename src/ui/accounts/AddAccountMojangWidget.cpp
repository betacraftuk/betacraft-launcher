#include "AddAccountMojangWidget.h"

#include <QtWidgets>

extern "C" {
	#include "../../core/AuthMojang.h"
	#include "../../core/Betacraft.h"
}

AddAccountMojangWidget::AddAccountMojangWidget(QWidget* parent)
	: QWidget{ parent } {
	char tr[256];

	_layout = new QGridLayout(this);
	_emailLabel = new QLabel(this);
	_passwordLabel = new QLabel(this);
	_emailEdit = new QLineEdit(this);
	_passwordEdit = new QLineEdit(this);
	_signInButton = new QPushButton(this);

	bc_translate("accounts_mojang_email", tr);
	_emailLabel->setText(QString(tr));
	bc_translate("accounts_mojang_password", tr);
	_passwordLabel->setText(QString(tr));

	QFont font;
	font.setPointSize(12);

	_passwordEdit->setEchoMode(QLineEdit::Password);
	bc_translate("accounts_mojang_signin_button", tr);
	_signInButton->setText(QString(tr));

	_emailLabel->setFont(font);
	_passwordLabel->setFont(font);
	_signInButton->setFont(font);

	_layout->setAlignment(Qt::AlignVCenter);

	_layout->addWidget(_emailLabel, 0, 0, 1, 1);
	_layout->addWidget(_passwordLabel, 1, 0, 1, 1);
	_layout->addWidget(_emailEdit, 0, 1, 1, 10);
	_layout->addWidget(_passwordEdit, 1, 1, 1, 10);
	_layout->addWidget(_signInButton, 2, 0, 1, 11);

	_layout->setSpacing(8);
	_layout->setContentsMargins(50, 0, 50, 0);

	setStyleSheet("QLineEdit { padding: 3px; }");

	setLayout(_layout);

	connect(_signInButton, SIGNAL(released()), this, SLOT(Authenticate()));
}

void AddAccountMojangWidget::Authenticate() {
	if (!_emailLabel->text().isEmpty() && !_passwordLabel->text().isEmpty()) {
		bc_auth_mojang(_emailLabel->text().toStdString().c_str(), _passwordLabel->text().toStdString().c_str());
		emit signal_accountAddSuccess();
	}
}
