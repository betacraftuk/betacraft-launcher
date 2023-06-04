#ifndef ABOUTWIDGET_H
#define ABOUTWIDGET_H

#include <QWidget>

class QGridLayout;
class QLabel;
class QTabWidget;
class QTextBrowser;

class AboutWidget : public QWidget {
    Q_OBJECT
public:
    explicit AboutWidget(QWidget *parent = nullptr);

private:
    QGridLayout* _layout;
    QLabel* _logo;
    QTabWidget* _menu;
    QGridLayout* _aboutSectionLayout;
    QGridLayout* _creditsSectionLayout;
    QGridLayout* _licenseSectionLayout;
    QWidget* _aboutSection;
    QWidget* _creditsSection;
    QWidget* _licenseSection;
    QLabel* _links;
    QTextBrowser* _licenseList;
    QTextBrowser* _creditsList;
};

#endif // ABOUTWIDGET_H
