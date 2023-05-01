#ifndef ADDACCOUNTMOJANGWIDGET_H
#define ADDACCOUNTMOJANGWIDGET_H

#include <QWidget>
#include <QtConcurrent>

class QGridLayout;
class QLabel;
class QLineEdit;
class QPushButton;

class AddAccountMojangWidget : public QWidget
{
	Q_OBJECT
public:
	explicit AddAccountMojangWidget(QWidget* parent = nullptr);

private slots:
	void Authenticate();

signals:
	void signal_accountAddSuccess();

private:
	QGridLayout* _layout;
	QLabel* _emailLabel;
	QLabel* _passwordLabel;
	QLineEdit* _emailEdit;
	QLineEdit* _passwordEdit;
	QPushButton* _signInButton;
};

#endif
