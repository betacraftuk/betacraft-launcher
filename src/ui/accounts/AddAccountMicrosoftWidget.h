#ifndef ADDACCOUNTMICROSOFTWIDGET_H
#define ADDACCOUNTMICROSOFTWIDGET_H

#include <QWidget>
#include <QtConcurrent>

class QGridLayout;
class QLabel;

class AddAccountMicrosoftWidget : public QWidget {
	Q_OBJECT
public:
	explicit AddAccountMicrosoftWidget(QWidget* parent = nullptr);

protected:
	void showEvent(QShowEvent* event);

private slots:
	void Authenticate();

signals:
	void signal_accountAddSuccess();

private:
	QGridLayout* _layout;
	QLabel* _microsoftLink;
	QLabel* _code;
	QLabel* _proceedText;
	QLabel* _typeCodeText;
	QFutureWatcher<void> _watcher;
};

#endif
