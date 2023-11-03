#ifndef SETTINGSGENERALWIDGET_H
#define SETTINGSGENERALWIDGET_H

#include <QWidget>

extern "C" {
    #include "../../core/Settings.h"
}

class QGridLayout;
class QGroupBox;
class QComboBox;
class QVBoxLayout;
class QCheckBox;

class SettingsGeneralWidget : public QWidget
{
    Q_OBJECT
public:
    explicit SettingsGeneralWidget(QWidget* parent = nullptr);

private slots:
    void onLanguageChange(const QString& lang);
    void onThemeChange(const QString& theme);
    void onDiscordCheckboxClicked(bool checked);

signals:
    void signal_toggleDiscordRPC();

private:
    QGridLayout* _layout;
    QVBoxLayout* _languageLayout;
    QGroupBox* _languageGroup;
    QVBoxLayout* _themeLayout;
    QGroupBox* _themeGroup;
    QVBoxLayout* _discordLayout;
    QGroupBox* _discordGroup;
    QComboBox* _languageCombo;
    QComboBox* _themeCombo;
    QCheckBox* _discordCheckbox;
};

#endif
