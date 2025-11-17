public interface Interactable {
    // primary interaction - shift key
    void onInteractionStart(Player player, CodeBlue game);
    void onInteractionUpdate(Player player, double deltaTime);
    void onInteractionEnd(Player player);
    boolean canInteract(Player player);
    String getInteractionPrompt();

    // secondary interaction - ctrl key
    boolean canUse(Player player, CodeBlue game);
    void onUse(Player player, CodeBlue game);
}
