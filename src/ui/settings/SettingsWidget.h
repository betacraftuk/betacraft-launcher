#ifndef SETTINGSWIDGET_H
#define SETTINGSWIDGET_H

#include <QWidget>

#include "SettingsGeneralWidget.h"
#include "SettingsJavaWidget.h"

class QGridLayout;
class QListWidget;
class QStackedWidget;
class QListWidgetItem;

class SettingsWidget : public QWidget
{
	Q_OBJECT
public:
	explicit SettingsWidget(QWidget* parent = nullptr);
	void downloadRecommendedJava(QString javaVersion);
	void setRecommendedJava(QString javaPath);

private slots:
	void onSidebarItemClicked(QListWidgetItem* item);

signals:
	void signal_toggleTabs();

private:
	QGridLayout* _layout;
	QListWidget* _sidebar;
	QStackedWidget* _menu;
	SettingsGeneralWidget* _settingsGeneralWidget;
	QListWidgetItem* _settingsGeneralItem;
	SettingsJavaWidget* _settingsJavaWidget;
	QListWidgetItem* _settingsJavaItem;
};

#endif
