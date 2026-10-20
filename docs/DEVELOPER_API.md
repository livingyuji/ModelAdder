# Model Adder — Developer API Guide

Model Adder exposes a clean, decoupled public API in the `model-adder-api` artifact.

---

## 1. Getting the API Instance

```java
import com.modeladder.api.ModelAdderAPI;

ModelAdderAPI api = ModelAdderAPI.get();
```

---

## 2. Checking & Creating Custom Items

```java
import com.modeladder.api.content.ContentKey;
import org.bukkit.inventory.ItemStack;

// Check if an item is a custom item
if (api.isCustomItem(itemStack)) {
    ContentKey key = api.getItemKey(itemStack).orElseThrow();
    player.sendMessage("Custom Item Key: " + key.asString());
}

// Create a custom item stack programmatically
Optional<ItemStack> rubySwordOpt = api.createItemStack("modeladder:ruby_sword", 1);
rubySwordOpt.ifPresent(stack -> player.getInventory().addItem(stack));
```

---

## 3. Listening to Custom Events

```java
import com.modeladder.api.event.CustomItemUseEvent;
import com.modeladder.api.event.CustomBlockPlaceEvent;
import com.modeladder.api.event.ResourcePackBuildEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MyPluginListener implements Listener {

    @EventHandler
    public void onCustomItemUse(CustomItemUseEvent event) {
        if (event.getCustomItem().getKey().asString().equals("modeladder:ruby_sword")) {
            event.getPlayer().sendMessage("You swung the legendary Ruby Sword!");
        }
    }

    @EventHandler
    public void onPackBuild(ResourcePackBuildEvent event) {
        if (event.isSuccess()) {
            Bukkit.getLogger().info("Pack build finished! SHA1: " + event.getSha1Hash());
        }
    }
}
```
