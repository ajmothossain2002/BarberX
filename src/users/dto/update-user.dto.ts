import { IsString, IsOptional, IsPhoneNumber } from 'class-validator';

export class UpdateUserDto {
    @IsString()
    @IsOptional()
    name?: string;

    @IsPhoneNumber()
    @IsOptional()
    phone?: string;
}
