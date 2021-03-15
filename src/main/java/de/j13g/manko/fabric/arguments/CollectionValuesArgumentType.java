package de.j13g.manko.fabric.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.LiteralText;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CollectionValuesArgumentType<T> implements ArgumentType<T> {

    private final Collection<T> collection;

    public CollectionValuesArgumentType(Collection<T> collection) {
        this.collection = collection;
    }

    public static <T> CollectionValuesArgumentType<T> collection(Collection<T> collection) {
        return new CollectionValuesArgumentType<>(collection);
    }

    public static <S, T> T getValue(String name, Class<T> type,  CommandContext<S> context) {
        return context.getArgument(name, type);
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {

        int start = reader.getCursor();
        if (!reader.canRead())
            reader.skip();

        while (reader.canRead() && reader.peek() != ' ')
            reader.skip();

        String input = reader.getString().substring(start, reader.getCursor());
        for (T value : collection)
            if (input.equals(value.toString()))
                return value;

        throw new SimpleCommandExceptionType(new LiteralText("Invalid option"))
                .createWithContext(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(collection.stream().map(Object::toString), builder);
    }
}
