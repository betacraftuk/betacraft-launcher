#ifndef CONSOLELOGWIDGET_H
#define CONSOLELOGWIDGET_H

#include <QWidget>

class QGridLayout;
class QTextEdit;
class QTimer;
class QLineEdit;
class QPushButton;

class ConsoleLogWidget : public QWidget
{
    Q_OBJECT
public:
    explicit ConsoleLogWidget(QWidget *parent = nullptr);

private slots:
    void UpdateConsoleLog();
    void pauseConsoleLog();
    void clearConsoleLog();

protected:
    void showEvent(QShowEvent* e);
    void closeEvent(QCloseEvent* e);
    void keyPressEvent(QKeyEvent* e);

private:
    QGridLayout* _layout;
    QTextEdit* _consoleLog;
    QTimer* _consoleLogTimer;
    QLineEdit* _searchBar;
    QPushButton* _clearButton;
    QPushButton* _pauseButton;
    QPushButton* _copyButton;
    void initObjects();
    void connectSignalsToSlots();
    void initLayout();
};

#endif // CONSOLELOGWIDGET_H
