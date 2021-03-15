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

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class EnumValuesArgumentType<T extends Enum<T>> implements ArgumentType<T> {

    private final String[] stringValues;
    private final HashMap<String, T> reverseMap;

    public EnumValuesArgumentType(Class<T> enumeration) {
        T[] values = enumeration.getEnumConstants();
        stringValues = Arrays.stream(values).map(Enum::toString).toArray(String[]::new);

        reverseMap = new HashMap<>(values.length);
        for (int i = 0; i < values.length; ++i)
            reverseMap.put(stringValues[i], values[i]);
    }

    public static <T extends Enum<T>> EnumValuesArgumentType<T> enumValues(Class<T> enumeration) {
        return new EnumValuesArgumentType<>(enumeration);
    }

    public static <S, T extends Enum<T>> T getEnum(String name, Class<T> enumeration, CommandContext<S> context) {
        return context.getArgument(name, enumeration);
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {

        int start = reader.getCursor();
        if (!reader.canRead())
            reader.skip();

        while (reader.canRead() && reader.peek() != ' ')
            reader.skip();

        String enumValue = reader.getString().substring(start, reader.getCursor());
        if (!reverseMap.containsKey(enumValue))
            throw new SimpleCommandExceptionType(new LiteralText("Invalid option"))
                    .createWithContext(reader);

        return reverseMap.get(enumValue);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(stringValues, builder);
    }
}
