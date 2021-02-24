mvn -B package --file pom.xml
file=$(find target -type f -name "TradeSystem_v*.jar" | head -n 1)
name=${file[@]//.jar/}
name=${name:7}
info=${name##*_}
version=${name:12}
version=${version[@]//_$info/}
if [ "$info" = "$version" ]; then
	info="New release**__   -   __**"
else
	info=${info[@]//-/ }
	info="$info**__   -   __**"
fi
NOW="$(LC_ALL=en_GB.utf8 date --universal)"
milestone=15
hook="https://discord.com/api/webhooks/763794920602730506/UZHg6WmD_ei_7DVYSt1DNU0uOMIlbbRQXhbV9THMP4eIz_SGiSxgpvMgYlW4aUF27Uwe"
echo "Sending '$file' to 'DevBuilds' channel, this may take a while..."
curl -i -H 'Expect: application/json' -F file=@"$file" -F "payload_json={ \"wait\": true, \"avatar_url\":\"https://i.imgur.com/x3pSH55.png\", \"content\": \"@here\n__**$info$version**__   [$NOW]\n\n**Changelog:**\n<https://github.com/CodingAir/TradeSystem/milestone/$milestone?closed=1>\", \"username\": \"TradeSystem CI\" }" $hook