#!/bin/bash
# Simule exactement votre workflow manuel
/usr/local/tomee/bin/catalina.sh run &  # Démarre TomEE en arrière-plan
tail -f /usr/local/tomee/logs/catalina.out  # Affiche les logs comme en local