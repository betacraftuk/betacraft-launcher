#include "InstanceEditServerWidget.h"

#include "../Betacraft.h"
#include <QtWidgets>

InstanceEditServerWidget::InstanceEditServerWidget(QWidget* parent)
    : QWidget{ parent }
{
    char tr[256];

    _layout = new QGridLayout();
    _serverAddressGroup = new QGroupBox();
    _serverIpTextEdit = new QLineEdit();
    _serverPortTextEdit = new QLineEdit();
    _serverAddressLayout = new QGridLayout();
    _joinServerCheckbox = new QCheckBox(bc_translate("instance_server_join_server_box"), this);

    _serverIpTextEdit->setPlaceholderText("Server IP");
    _serverPortTextEdit->setPlaceholderText("Port (25565 by default)");

    _serverAddressGroup->setTitle(bc_translate("instance_server_join_group_title"));

    _serverAddressLayout->addWidget(_joinServerCheckbox, 0, 0, 1, 1);
    _serverAddressLayout->addWidget(_serverIpTextEdit, 1, 0, 1, 1);
    _serverAddressLayout->addWidget(_serverPortTextEdit, 2, 0, 1, 1);
    _serverAddressGroup->setLayout(_serverAddressLayout);

    _layout->addWidget(_serverAddressGroup, 0, 0, 1, 1);

    _layout->setAlignment(Qt::AlignTop);

    _layout->setSpacing(5);
    _layout->setContentsMargins(10, 10, 10, 10);

    setStyleSheet("QLabel { font-size: 14px; }");
    setLayout(_layout);
}

void InstanceEditServerWidget::setInstance(bc_instance instance) {
    _joinServerCheckbox->setChecked(instance.join_server);
    _serverIpTextEdit->setText(instance.server_ip);
    _serverPortTextEdit->setText(instance.server_port);
}

bc_instance* InstanceEditServerWidget::getSettings() {
    bc_instance* instance = new bc_instance();

    instance->join_server = _joinServerCheckbox->isChecked();
    snprintf(instance->server_ip, sizeof(instance->server_ip), "%s", _serverIpTextEdit->text().toStdString().c_str());
    snprintf(instance->server_port, sizeof(instance->server_port), "%s", _serverPortTextEdit->text().toStdString().c_str());

    return instance;
}
