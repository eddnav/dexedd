package com.nav.dexedd.persistence.access;

import android.content.Context;
import android.database.Cursor;

import com.nav.dexedd.R;
import com.nav.dexedd.model.*;
import com.nav.dexedd.persistence.DexDatabase;
import com.nav.dexedd.util.PokemonText;

import java.util.ArrayList;
import java.util.List;

/**
 * Dex entry data access object
 *
 * @author Eduardo Naveda
 * @since 0.0.1
 */
public class DexEntry extends Access {

    public static enum Version {

        XY(24); // Todo add all versions

        private Integer version;

        private Version(Integer version) {
            this.version = version;
        }

        @Override
        public String toString() {
            return version.toString();
        }

    }


    private Integer pokemonId;
    private Version version;

    private DexEntry(Context context, Integer pokemonId, Version version) {
        super(context);
        this.pokemonId = pokemonId;
        this.version = version;
    }

    /**
     * Creates a dex entry.
     *
     * @param context   The application context
     * @param pokemonId The Pokémon id
     *
     * @return A dex entry object
     */
    public static DexEntry create(Context context, Integer pokemonId) {
        database = DexDatabase.getInstance(context).getReadableDatabase();
        Version version = Version.XY; // Todo from preferences
        return new DexEntry(context, pokemonId, version);
    }

    /**
     * Creates a dex entry.
     *
     * @param context   The application context
     * @param pokemonId The Pokémon id
     * @param version   The game version
     *
     * @return A dex entry object
     */
    public static DexEntry create(Context context, Integer pokemonId, Version version) {
        database = DexDatabase.getInstance(context).getReadableDatabase();
        return new DexEntry(context, pokemonId, version);
    }

    /**
     * Get the Pokémon information for this dex entry.
     *
     * @return The {@link com.nav.dexedd.model.Pokemon} for the dex entry
     */
    public Pokemon getPokemon() {
        String[] args = {Dex.DexType.NATIONAL_DEX.toString(), version.toString(), pokemonId.toString()};
        String query = getContext().getString(R.string.get_dex_entry);
        Cursor cursor = database.rawQuery(query, args);
        cursor.moveToFirst();
        Pokemon pokemon = new Pokemon();
        pokemon.setId(cursor.getInt(0));
        pokemon.setSpeciesId(cursor.getInt(1));
        pokemon.setDexNumber(cursor.getInt(2));
        pokemon.setName(cursor.getString(3));
        pokemon.setGenus(cursor.getString(4));
        pokemon.setFlavorText(PokemonText.cleanDexText(cursor.getString(5)));
        Type primaryType = new Type();
        primaryType.setId(cursor.getInt(6));
        pokemon.setPrimaryType(primaryType);
        Integer secondaryTypeId = cursor.getInt(7);
        if (secondaryTypeId != 0) {
            Type secondaryType = new Type();
            secondaryType.setId(secondaryTypeId);
            pokemon.setSecondaryType(secondaryType);
        }
        pokemon.setAbilities(getAbilities(pokemon.getId()));
        pokemon.setCatchRate(cursor.getInt(8));
        pokemon.setEggGroups(getEggGroups(pokemon.getSpeciesId()));
        pokemon.setGenderRatio(cursor.getDouble(9));
        // Height from the data source is measured in decameters (dam), thus the conversion to meters
        pokemon.setHeight((double) cursor.getInt(10) / 10);
        // Weight from the data source is measured in hectograms (hg), thus the conversion to kilograms
        pokemon.setWeight((double) cursor.getInt(11) / 10);
        pokemon.setStats(getStats(pokemon.getId()));
        pokemon.setCatched(cursor.getInt(12) == 1);
        cursor.close();
        return pokemon;
    }

    /**
     * Get a Pokémon's abilities.
     *
     * @param pokemonId The Pokémon id
     *
     * @return The ability list for the Pokémon
     */
    public List<Ability> getAbilities(Integer pokemonId) {
        String[] args = {pokemonId.toString()};
        String query = getContext().getString(R.string.get_abilities);
        Cursor cursor = database.rawQuery(query, args);
        List<Ability> abilities = new ArrayList<>();
        while (cursor.moveToNext()) {
            Ability ability = new Ability();
            ability.setId(cursor.getInt(0));
            ability.setName(cursor.getString(1));
            ability.setFlavorText(PokemonText.cleanDexText(cursor.getString(2)));
            ability.setEffect(PokemonText.cleanDexText(cursor.getString(3)));
            ability.setIsHidden(cursor.getInt(4) == 1);
            ability.setSlot(cursor.getInt(5));
            abilities.add(ability);
        }
        cursor.close();
        return abilities;
    }

    /**
     * Get a Pokémon's egg groups.
     *
     * @param speciesId The Pokémon species id
     *
     * @return The egg group list for the Pokémon species
     */
    public List<EggGroup> getEggGroups(Integer speciesId) {
        String[] args = {pokemonId.toString()};
        String query = getContext().getString(R.string.get_egg_groups);
        Cursor cursor = database.rawQuery(query, args);
        List<EggGroup> eggGroups = new ArrayList<>();
        while (cursor.moveToNext()) {
            EggGroup eggGroup = new EggGroup();
            eggGroup.setId(cursor.getInt(0));
            eggGroup.setName(cursor.getString(1));
            eggGroups.add(eggGroup);
        }
        cursor.close();
        return eggGroups;
    }

    /**
     * Get a Pokémon's stats.
     *
     * @param pokemonId The Pokémon id
     *
     * @return The Stats object for the Pokémon
     */
    public Stats getStats(Integer pokemonId) {
        String[] args = {pokemonId.toString()};
        String query = getContext().getString(R.string.get_stats);
        Cursor cursor = database.rawQuery(query, args);
        Stats stats = new Stats();
        while (cursor.moveToNext()) {
            Stat stat = new Stat(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2));
            stats.setStat(stat, stat.getId());
        }
        cursor.close();
        return stats;
    }
}
