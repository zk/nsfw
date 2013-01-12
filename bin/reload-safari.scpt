tell application "Safari"
    set windowList to every window
    repeat with aWindow in windowList
	set tabList to every tab of aWindow
	repeat with atab in tabList
		if (URL of atab contains "localhost") then
		  tell atab to do javascript "window.location.reload()"
		end if
	end repeat
    end repeat
end tell