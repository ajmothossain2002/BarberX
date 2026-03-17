import { IsString, IsDateString } from 'class-validator';

export class CreateBookingDto {
    @IsString()
    shopId: string;

    @IsString()
    serviceId: string;

    @IsDateString()
    startTime: string; // ISO string, e.g., "2025-04-01T10:00:00Z"
}
