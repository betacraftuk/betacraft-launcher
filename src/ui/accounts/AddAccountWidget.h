#ifndef ADDACCOUNTWIDGET_H
#define ADDACCOUNTWIDGET_H

#include <QWidget>

class QGridLayout;
class QTabWidget;

class AddAccountWidget : public QWidget
{
    Q_OBJECT
public:
    explicit AddAccountWidget(QWidget* parent = nullptr);

signals:
    void signal_accountAddSuccess();

private: 
    QGridLayout* _layout;
    QTabWidget* _menu;
    QWidget* _microsoftWidget;
    QWidget* _mojangWidget;
};

#endif
